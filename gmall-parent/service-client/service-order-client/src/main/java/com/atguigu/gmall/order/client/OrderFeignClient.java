package com.atguigu.gmall.order.client;


import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@FeignClient("service-order")
public interface OrderFeignClient {

    //交易号
    @GetMapping("/api/order/inner/getTradeNo")
    public String getTradeNo();

    //根据订单ID查询订单信息
    @GetMapping("/api/order/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfoById(@PathVariable(name = "orderId") Long orderId);
}
