package com.atguigu.gmall.task.mq;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀活动 将秒杀商品压入缓存
 */
@Component
@EnableScheduling
public class Task1 {


    @Autowired
    private RabbitService rabbitService;

    //定时任务    每天半夜 0点
    //@Scheduled(cron = "* * 0 * * ?")
    //测试使用   每30秒执行一次
    @Scheduled(cron = "0/30 * * * * ?")
    public void taskMq(){
        //发消息
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_1,"111");

    }
    //晚20点清理当天的结束的秒杀缓存
    @Scheduled(cron = "* * 20 * * ?")
    public void task2(){
        //发消息  五大数据类型： 简单模式  队列      工作模式  队列  消费者接收（竞争 关系
        rabbitService.sendMessage(MqConst.QUEUE_TASK_20,"111");

    }
//    /**
//     * 每天凌晨1点执行
//     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void task1() {
//        rabbitService.sendMessage(MqCons.EXCHANGE_DIRECT_TASK, MqCons.ROUTING_TASK_1, "");
//    }
}
