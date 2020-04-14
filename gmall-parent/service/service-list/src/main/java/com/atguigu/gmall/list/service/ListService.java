package com.atguigu.gmall.list.service;

public interface ListService {
    void upperGoods(Long skuId);

    void lowerGoods(Long skuId);

    void incrHotScore(Long skuId);
}
