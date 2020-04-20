package com.atguigu.gmall.order.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@FeignClient("service-order")
public interface OrderFeignClient {

    //交易号
    @GetMapping("/api/order/inner/getTradeNo")
    public String getTradeNo();
}
