package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.pojo.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description 消息发送确认
 * <p>
 * ConfirmCallback  只确认消息是否正确到达 Exchange 中
 * ReturnCallback   消息没有正确到达队列时触发回调，如果正确到达队列不执行
 * <p>
 * 1. 如果消息没有到exchange,则confirm回调,ack=false
 * 2. 如果消息到达exchange,则confirm回调,ack=true
 * 3. exchange到queue成功,则不回调return
 * 4. exchange到queue失败,则回调return
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;


    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }

    //交换机应答方法   不管成功或失败 下面的方法都执行
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送成功：" + JSON.toJSONString(correlationData));
        } else {
            log.info("消息发送失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));

            //消息发送失败：
            // channel error; protocol method:
            // #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange
            // 'exchange.111' in vhost '/', class-id=60, method-id=40)
            // 数据：null
            /////////////////////////封装发消息的公共实现类之后/////////////////////////////////
            //数据：{"delay":false,"delayTime":10,
            // "exchange":"exchange.111",
            // "future":{"cancelled":false,"done":true},
            // "id":"83d436d2-ed74-4eb5-abc2-a9bb985c0a58",
            // "message":"大家好才是真的好","
            // retryCount":0,
            // "routingKey":""}

            //交换机应答失败  需要重新发送消息
            this.retryMessage(correlationData);

        }
    }

    //重新发送方法
    public void retryMessage(CorrelationData correlationData) {
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;
        //0:判断是否发送不超过3次  除了第一次发送以外 重新发送2次即可 不超过3次
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount < 2) {
            retryCount++;
            //更新重发次数   （次数  交换机失败重发次数）
            gmallCorrelationData.setRetryCount(retryCount);
            //更新重发次数   (次数   队列失败重发次数）
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),JSON.toJSONString(gmallCorrelationData));
            log.info("第{}次重新发送",retryCount);
            //1：交换机
            //2;routingKey
            //3:消息体
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange()
                    , gmallCorrelationData.getRoutingKey(), gmallCorrelationData.getMessage()
                    , gmallCorrelationData);
        }
    }


    //队列应答方法  只有失败的时候才应答
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        // 反序列化对象输出
//        System.out.println("消息主体: " + new String(message.getBody()));
//        System.out.println("应答码: " + replyCode);
//        System.out.println("描述：" + replyText);
//        System.out.println("消息使用的交换器 exchange : " + exchange);
//        System.out.println("消息使用的路由键 routing : " + routingKey);

        //Message对象如果是NULL 不要执行了
        if(null == message){
            return;
        }
        //1:获取到CorrelationData的UUID
        String correlationDataUUID = message.getMessageProperties()
                .getHeader("spring_returned_message_correlation");
        if(null == correlationDataUUID){
            return;
        }
        //2:Redis缓存  根据correlationDataUUID从缓存中获取出CorrelationData对象
            //-- 2步中的缓存中CorrelationData对象 可以在发消息的时候设置到缓存中
        GmallCorrelationData gmallCorrelationData = JSON.parseObject((String) redisTemplate.
                opsForValue().get(correlationDataUUID), GmallCorrelationData.class);
        //判断是否为延迟消息
        if(gmallCorrelationData.isDelay()){
            return;
        }
        log.info("队列发送失败");
        //3：重新发送
        retryMessage(gmallCorrelationData);
    }

}