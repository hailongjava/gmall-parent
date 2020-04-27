package com.atguigu.gmall.activity.listener;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 秒杀监听器
 */
@Component
public class AddGoodsToRedis {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    //接收消息 将Mysql中的当天的商品压入缓存中
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            value = @Queue(value = MqConst.QUEUE_TASK_1),
            key = MqConst.ROUTING_TASK_1
    ))
    public void receiverMessage(Message message, Channel channel) {
        try {
            //0:查询当天的秒杀商品  //开始时间必须是今天、审核状态必须1 、 剩余库存数量大于0
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
            //时间  今天   年月日  不要时分秒
            queryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')",
                    DateUtil.formatDate(new Date()));
            //审核状态为1
            queryWrapper.eq("status", "1");
            //剩余库存数量大于0
            queryWrapper.gt("stock_count", 0);
            //执行查询
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(seckillGoodsList)) {
                //在缓存中已经有了的秒杀商品 就不再缓存一次了
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    if (!redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_GOODS,
                            seckillGoods.getSkuId().toString())) {
                        //1、提前缓存秒杀商品   每一个商品  主键  skuId
                        //参数1： Map seckill:goods = new HashMap   Map的名称
                        //参数2： m.put(k,v)    k  skuId String
                        //参数3： v  goods
                        redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS
                                , seckillGoods.getSkuId().toString(), seckillGoods);
                        //            2、提前将秒杀商品的库存数缓存到Redis（防止超卖）
                        String[] ids = buildGoodsIds(seckillGoods);
                        // List seckill:stock = new ArrayList();
                        //参数1：列表名称
                        //参数2： 大量的商品ID、集合
                        redisTemplate.opsForList().leftPushAll(
                                RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId(), ids);
                        //            3、提前准备状态位  skuId:1
                        redisTemplate.convertAndSend("seckillpush",
                                seckillGoods.getSkuId() + ":1");// skuId:1   接收 "6:1"
                    }
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //构建  商品ID  商品数量  返回值数组  商品数量的商品ID
    private String[] buildGoodsIds(SeckillGoods seckillGoods) {
        Long skuId = seckillGoods.getSkuId();
        Integer stockCount = seckillGoods.getStockCount();

        String[] ids = new String[stockCount];
        for (Integer i = 0; i < stockCount; i++) {
            ids[i] = String.valueOf(skuId);
        }
        return ids;
    }
}
