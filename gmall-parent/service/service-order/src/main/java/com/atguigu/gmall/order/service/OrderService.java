package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {
    String getTradeNo(String userId);

    boolean hasStock(Long skuId, Integer skuNum);

    void sumbitOrder(OrderInfo orderInfo);
}
