package com.atguigu.gmall.activity.listener;

import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 接收秒杀抢购的消息
 *
 */
@Component
public class SeckillReceiver {


    @Autowired
    private RedisTemplate redisTemplate;


    //接收消息
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER),
            key = MqConst.ROUTING_SECKILL_USER
    ))
    public void receiverMessage(UserRecode userRecode, Message message, Channel channel){
        try {
            //用户ID   库存ID
            System.out.println("开始抢购的消息：" + userRecode.toString());

            //1、状态位（发生变化）
            Object o = CacheHelper.get(String.valueOf(userRecode.getSkuId()));
            if(null == o || o.equals("0")){
                //商品已售完
                return;
            }
            //2、同一个用户只可以买一次
            // 在本次秒杀活动中  10款  此用户不管什么秒杀商品 只能买一次
//            redisTemplate.opsForValue().setIfAbsent(
//                    RedisConst.SECKILL_USER + userRecode.getUserId(),
//                    userRecode.getSkuId());
            // 在本次秒杀活动中  10款  此用户每一个秒杀商品 只能买一次  一小时之后可以再买
            Boolean isExist = redisTemplate.opsForValue().setIfAbsent(
                    RedisConst.SECKILL_USER + userRecode.getUserId() + userRecode.getSkuId()
                    , userRecode.getSkuId(), RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
            if(!isExist){
                //存在了
                return;
            }
            //3、判断库存
            Object rightPop = redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX +
                    userRecode.getSkuId());
            if(null == rightPop){
                //商品已售完
                //更新状态位
                redisTemplate.convertAndSend("seckillpush",
                        userRecode.getSkuId() + ":0");
                return;
            }
            //4:抢购到商品了
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash()
                    .get(RedisConst.SECKILL_GOODS, userRecode.getSkuId().toString());
            //Key 用户ID
            //Value ： 抢购资格的对象
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setUserId(userRecode.getUserId());
            orderRecode.setNum(1);
            orderRecode.setSeckillGoods(seckillGoods);
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDERS + userRecode.getUserId(),
                    orderRecode,30,TimeUnit.MINUTES);
            //后续.....
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
