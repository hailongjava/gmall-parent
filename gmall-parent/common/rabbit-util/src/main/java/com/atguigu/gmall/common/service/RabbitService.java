package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.pojo.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMq发消息 公共实现类
 */
@Component
@SuppressWarnings("all")
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //发送延迟消息
    public boolean sendDelayMessage(String exchange,String routingKey,Object msg,int delayTime){
        //发送失败之后 回调的返回值对象
        GmallCorrelationData correlationData = new GmallCorrelationData();
        //ID
        correlationData.setId(UUID.randomUUID().toString());
        //交换机
        correlationData.setExchange(exchange);
        //路由Key
        correlationData.setRoutingKey(routingKey);
        //消息
        correlationData.setMessage(msg);
        //是延迟
        correlationData.setDelay(true);
        //设置延迟的时间  设置二小时
        //correlationData.setDelayTime(1000*60*60*2);
        correlationData.setDelayTime(delayTime);
        //防止消息 不能送达到队列  失败答应时  需要从缓存中获取correlationData对象
        // 将correlationData设置到缓存中
        redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(correlationData));
        //是否设置存活时间  5分钟 之内是可以完成发送三次的 完全够了
        //发消息
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,t -> {
            //局部设置延迟时间
            t.getMessageProperties().setDelay(correlationData.getDelayTime());
            return t;
        } ,correlationData);
        return true;
    }



    //发消息(普通消息）
    public boolean sendMessage(String exchange,String routingKey,Object msg){

        //发送失败之后 回调的返回值对象
        GmallCorrelationData correlationData = new GmallCorrelationData();
        //ID
        correlationData.setId(UUID.randomUUID().toString());
        //交换机
        correlationData.setExchange(exchange);
        //路由Key
        correlationData.setRoutingKey(routingKey);
        //消息
        correlationData.setMessage(msg);
        //不是延迟  是正常消息
        correlationData.setDelay(false);
        //防止消息 不能送达到队列  失败答应时  需要从缓存中获取correlationData对象
        // 将correlationData设置到缓存中
        redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(correlationData));
        //是否设置存活时间  5分钟 之内是可以完成发送三次的 完全够了
        //发消息
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,correlationData);
        return true;
    }
    //发消息(普通消息）
    public boolean sendMessage(String routingKey,Object msg){

        //发送失败之后 回调的返回值对象
        GmallCorrelationData correlationData = new GmallCorrelationData();
        //ID
        correlationData.setId(UUID.randomUUID().toString());
        //路由Key
        correlationData.setRoutingKey(routingKey);
        //消息
        correlationData.setMessage(msg);
        //不是延迟  是正常消息
        correlationData.setDelay(false);
        //防止消息 不能送达到队列  失败答应时  需要从缓存中获取correlationData对象
        // 将correlationData设置到缓存中
        redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(correlationData));
        //是否设置存活时间  5分钟 之内是可以完成发送三次的 完全够了
        //发消息
        rabbitTemplate.convertAndSend(routingKey,msg,correlationData);
        return true;
    }




}
