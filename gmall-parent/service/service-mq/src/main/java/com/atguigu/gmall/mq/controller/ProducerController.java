package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterConfig;
import com.atguigu.gmall.mq.config.DelayLetterConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息 生产者  Controller
 */
@RestController
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
//
//    //发布、订阅模式  发消息
//    @GetMapping("/sendMessage")
//    public Result sendMessage(String message){
//        //发消息   rountingKey 空串  发布、订阅模式   任何绑定此交换机的队列 都可以接收此消息
//        rabbitTemplate.convertAndSend(
//                "exchange.111","haha","大家好才是真的好");
//
//        return Result.ok();
//    }

    @Autowired
    private RabbitService rabbitService;

    //在企业上班 发消息
    @GetMapping("/sendMessage")
    public Result sendMessage(){
        rabbitService.sendMessage("exchange.2","haha.sfsf.sfsf","大家好才是真的好");
        return Result.ok();
    }

    //发消息   死信队列 来完成延迟
    @GetMapping("/sendDeadLetterMessage")
    public Result sendDeadLetterMessage(){


       rabbitTemplate.convertAndSend(
               DeadLetterConfig.exchange_dead,
               DeadLetterConfig.routing_dead_1,
               "我是延迟消息、你看我延迟了吗？"
                ,(t)-> {
                   //局部设置死信队列的过期时间  单位是毫秒
                   t.getMessageProperties().setExpiration("10000");
                  return  t;
               });

        return Result.ok();
    }
    //发消息  延迟插件来完成延迟消息的发送
    @GetMapping("/sendDelayMessage")
    public Result sendDelayMessage(){

        rabbitTemplate.convertAndSend(DelayLetterConfig.exchange_delay,DelayLetterConfig.routing_delay,
                "我是基于延迟插件的消息",(t) -> {
                    //局部设置延迟时间   单位是毫秒
                    t.getMessageProperties().setDelay(10000);
                    return t;
                });

        return Result.ok();
    }

}
