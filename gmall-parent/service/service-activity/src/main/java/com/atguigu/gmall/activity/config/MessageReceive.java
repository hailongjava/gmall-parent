package com.atguigu.gmall.activity.config;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义实现类   为了接收到Redis队列中消息  并处理此消息
 */
@Component
public class MessageReceive {



    //方法 处理此消息的方法  message:  skuId:1     "6:1"
    public void receiveMessage(String message){
        System.out.println("===接收到Redis的发布、订阅消息：" + message);
        String msg = message.replaceAll("\"", "");
        String[] s = msg.split(":");
        //将状态位保存到内存中
        CacheHelper.put(s[0],s[1]);
        //获取出来打印在控制台上
        System.out.println("内存中的状态位：" + CacheHelper.get(s[0]));
    }
}
