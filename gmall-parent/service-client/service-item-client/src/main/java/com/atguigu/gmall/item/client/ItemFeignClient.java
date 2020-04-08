package com.atguigu.gmall.item.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情微服务对外暴露的接口
 */
@FeignClient(name = "service-item")
public interface ItemFeignClient {

    //数据汇总
    @GetMapping("/api/item/{skuId}")
    public Map getItem(@PathVariable(name = "skuId") Long skuId);
}
