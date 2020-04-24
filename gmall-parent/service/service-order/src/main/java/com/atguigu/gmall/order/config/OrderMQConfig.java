package com.atguigu.gmall.order.config;

import com.atguigu.gmall.common.constant.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 延迟消息配置
 * 1、取消订单
 * 2、
 */
//@Configuration
public class OrderMQConfig {


    //创建延迟交换机
    @Bean
    public CustomExchange orderCancelExchange(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type","direct");
        return new CustomExchange(
                MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,"x-delayed-message",
                true,false,arguments);
    }

    @Bean
    public Queue orderCancelQueue(){
        return QueueBuilder.durable(MqConst.QUEUE_ORDER_CANCEL).build();
    }

    @Bean
    public Binding orderCancelBinding(){
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderCancelExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }

}
