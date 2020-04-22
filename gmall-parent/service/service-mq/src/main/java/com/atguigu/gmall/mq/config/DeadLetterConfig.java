package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建死信队列    Java配置类
 * 、
 */
@Configuration
public class DeadLetterConfig {


    //交换机 （正常的）
    public static final String exchange_dead = "exchange.dead";
    //交换机 （正常的） 与 //死信队列  绑定
    public static final String routing_dead_1 = "routing.dead.1";
    //交换机 （正常的）与 //正常队列 绑定
    public static final String routing_dead_2 = "routing.dead.2";
    //死信队列
    public static final String queue_dead_1 = "queue.dead.1";
    //正常队列
    public static final String queue_dead_2 = "queue.dead.2";

    //创建交换机  基于死信队列   三种：发布订阅交换机  定向交换机  主题交换机        自定义交换机
    @Bean
    public DirectExchange exchange(){
        return ExchangeBuilder.directExchange(exchange_dead).build();
    }
    //死信队列
    @Bean
    public Queue deadLetterQueue(){
        Map<String, Object> arguments = new HashMap<>();
        //死信队列过期时  转发的交换机是
        arguments.put("x-dead-letter-exchange", exchange_dead);
        arguments.put("x-dead-letter-routing-key", routing_dead_2);
        //全局配置过期2小时   局部配置过期时间 优先于全局   全局配置是10秒
        //arguments.put("x-message-ttl", 10 * 1000);
        return QueueBuilder.durable(queue_dead_1).withArguments(arguments).build();
    }

    //正常队列
    @Bean
    public Queue queue(){
        return QueueBuilder.durable(queue_dead_2).build();
    }

    //绑定  交换机与死信列队
    @Bean
    public Binding bindingDeadLetterQueueToExchange(){
        return BindingBuilder.bind(deadLetterQueue()).to(exchange()).with(routing_dead_1);
    }
    //绑定  交换机与正常列队
    @Bean
    public Binding bindingQueueToExchange(){
        return BindingBuilder.bind(queue()).to(exchange()).with(routing_dead_2);
    }

}
