package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.MqConst;
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
