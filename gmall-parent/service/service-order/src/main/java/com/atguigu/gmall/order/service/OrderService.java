package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {
    String getTradeNo(String userId);

    boolean hasStock(Long skuId, Integer skuNum);

    Long sumbitOrder(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfoById(Long orderId);
}
