package com.atguigu.gmall.product.client.impl;


import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

/**
 *  对外暴露的接口 降级后处理实现类如下
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {


    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }
}
