package com.atguigu.gmall.payment.listener;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 关闭交易
 */
@Component
public class ClosePaymentMessage {

    @Autowired
    private PaymentService paymentService;

    //接收延迟2小或4小时
    @RabbitListener(queues =  MqConst.QUEUE_PAYMENT_CLOSE)
    public void closePayment(Long orderId, Message message, Channel channel){
        try {
            System.out.println("订单ID：" + orderId);

            //关闭交易
            paymentService.closePayment(orderId);

            //手动应答
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            //手动应答

        }
    }
}
