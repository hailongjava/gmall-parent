package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
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
