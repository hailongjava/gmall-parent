package com.atguigu.gmall.mq.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于延迟插件的  发送延迟消息
 */
@Configuration
public class DelayLetterConfig {


    //延迟交换机
    public static final String exchange_delay = "exchange.delay";
    //正常  绑定时候的路由键
    public static final String routing_delay = "routing.delay";
    //正常 队列
    public static final String queue_delay_1 = "queue.delay.1";


    //创建延迟交换机
    @Bean
    public CustomExchange customExchange(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type","direct");
        return new CustomExchange(
                exchange_delay,"x-delayed-message",
                true,false,arguments);
    }
    //正常队列
    @Bean
    public Queue queue11(){
        return QueueBuilder.durable(queue_delay_1).build();
    }
    //绑定
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue11()).to(customExchange()).with(routing_delay).noargs();
    }
}
