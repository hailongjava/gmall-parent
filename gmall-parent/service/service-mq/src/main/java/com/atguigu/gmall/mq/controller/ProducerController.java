package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息 生产者  Controller
 */
@RestController
public class ProducerController {

//    @Autowired
//    private RabbitTemplate rabbitTemplate;
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
}
