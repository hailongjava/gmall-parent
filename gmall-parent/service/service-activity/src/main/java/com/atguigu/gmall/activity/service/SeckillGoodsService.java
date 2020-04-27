package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    List<SeckillGoods> list();

    SeckillGoods getItemById(String skuId);
}
