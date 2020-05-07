package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

public interface OrderService {
    String getTradeNo(String userId);

    boolean hasStock(Long skuId, Integer skuNum);

    Long sumbitOrder(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfoById(Long orderId);

    void updateOrder(Long orderId);

    void sendOrderStatus(Long orderId);

    public void updateOrder(Long orderId, ProcessStatus processStatus);

    List<OrderInfo> orderSplit(Long orderId, String wareSkuMap);

    //准备json
    public String initWareOrder(Long orderId);
    //准备json
    public Map<String,Object> initWareOrder(OrderInfo orderInfo);
}
