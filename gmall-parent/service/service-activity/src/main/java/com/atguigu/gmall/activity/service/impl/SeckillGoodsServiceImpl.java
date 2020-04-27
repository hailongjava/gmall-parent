package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀管理
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;
    //查询缓存中所有今天要秒杀的商品
    @Override
    public List<SeckillGoods> list() {
        return redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);
    }

    //查询缓存中今天的一个秒杀商品 显示在详情页面上
    @Override
    public SeckillGoods getItemById(String skuId) {
        return (SeckillGoods) redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS,skuId);
    }
}
