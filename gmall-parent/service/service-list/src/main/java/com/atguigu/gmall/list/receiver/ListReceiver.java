package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.ListService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收消息
 * 上架 索引
 * 下架 删除索引
 */
@Component
public class ListReceiver {

    @Autowired
    private ListService listService;
    //接收
    //上架
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS,autoDelete = "false",durable = "true"),
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,autoDelete = "false",durable = "true"),
            key = MqConst.ROUTING_GOODS_UPPER
    ))
    public void upperGoods(Long skuId, Message message, Channel channel){

        System.out.println("上架商品并保存索引：" + skuId);
        listService.upperGoods(skuId);

    }
    //下架
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS,autoDelete = "false",durable = "true"),
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,autoDelete = "false",durable = "true"),
            key = MqConst.ROUTING_GOODS_LOWER
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel){

        System.out.println("下单商品并删除索引：" + skuId);
        listService.lowerGoods(skuId);

    }
}
