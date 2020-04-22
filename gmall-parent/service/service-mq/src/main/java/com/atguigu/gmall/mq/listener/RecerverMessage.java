package com.atguigu.gmall.mq.listener;

import com.atguigu.gmall.mq.config.DeadLetterConfig;
import com.atguigu.gmall.mq.config.DelayLetterConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 接收消息
 */
@Component
public class RecerverMessage {
    //计数器
    private int count = 0;
    @Autowired
    private RedisTemplate redisTemplate;//进行记录次数

    //监听器接收消息
    @RabbitListener(queues = "queue.2")
    public void recerverMessage(Object msg, Message message, Channel channel) throws Exception{

        //手动应答  参数1：标记    参数2：是否删除队列的消息    true:删除队列中消息  false：不删除队列中的消息
        try {
            System.out.println(msg);
            //先抛异常
            int i = 1 / 0;
            //手动应答
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            //e.printStackTrace();
            //1:判断是否 第一次抛异常   是重新发送的吗？
            if (message.getMessageProperties().isRedelivered()) {
                //2:如果不是第一次抛异常  拒绝接收消息
                //参数1： 消息的标记（ID）
                //参数2：当前消息是否放回队列  true:放回队列  false:不放回队列
                System.out.println("第二次接收此消息：又抛异常了：" + msg);
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                //3:如果是第一次抛异常  重新接收消息
                //参数1： 消息的标记（ID）
                //参数2： 是否批量处理
                //参数3： 当前消息是否放回队列   true:放回队列  false:不放回队列
                System.out.println("第一次接收此消息：抛异常了：" + msg);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

            }


        }
    }

    //接收基于死信延迟消息
    @RabbitListener(queues = DeadLetterConfig.queue_dead_2)
    public void receiverDeadLetter(Message message, Channel channel) throws Exception{

        System.out.println(new String(message.getBody()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
    }
    //接收基于插件延迟消息
    @RabbitListener(queues = DelayLetterConfig.queue_delay_1)
    public void receiverDelay(Message message, Channel channel) throws Exception{

        System.out.println(new String(message.getBody()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
    }

}
