package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 商品微服务对外暴露的接口
 */
@FeignClient(name = "service-product",fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    //根据skuId 查询库存表
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId);

}
