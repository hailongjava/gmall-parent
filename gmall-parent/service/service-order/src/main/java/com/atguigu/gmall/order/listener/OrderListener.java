package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 监听订单的消息
 * 1、取消订单
 * 2、暂定
 */
@Component
public class OrderListener {

//    @RabbitListener(queues = "queue.1")
//    public void cancelOrder111(String message){
//        System.out.println(message);
//    }

    //订单Service实现类
    @Autowired
    private OrderService orderService;

    //接收减库存结果  如果成功了 更新订单状态 为
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrderStatus(String msgJson,Message message,Channel channel){

        Map map = JSON.parseObject(msgJson, Map.class);
        String orderId = (String) map.get("orderId");
        String status = (String) map.get("status");
        if("DEDUCTED".equals(status)){
            //减库存成功
            orderService.updateOrder(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
        }else{
            //减库存失败
            //远程调用其它仓库
            //1：补货 、2：人工客服
            orderService.updateOrder(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
        }
    }


    //接收消息  并 更新订单状态
    @RabbitListener(bindings = {@QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    autoDelete = "false",durable = "true"),
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,autoDelete = "false",durable = "true"),
            key = MqConst.ROUTING_PAYMENT_PAY
    )})
    public void updateOrder(Long orderId,Message message,Channel channel) {
        try {
            System.out.println("订单ID：" + orderId);
            //更新订单状态
            orderService.updateOrder(orderId);
            //发消息给物流系统进行减少库存
            orderService.sendOrderStatus(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            if(message.getMessageProperties().isRedelivered()){
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }else{
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


    //取消订单
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId,Message message, Channel channel){
        try {
            System.out.println("订单ID：" + orderId);
            //取消订单
            orderService.cancelOrder(orderId);
            //手动应答
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
