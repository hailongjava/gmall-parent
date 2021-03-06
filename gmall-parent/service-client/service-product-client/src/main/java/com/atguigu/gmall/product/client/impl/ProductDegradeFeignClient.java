package com.atguigu.gmall.product.client.impl;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *  对外暴露的接口 降级后处理实现类如下
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {

    @Override
    public List<Map> getBaseCategoryList() {
        return null;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return null;
    }

    @Override
    public List<SkuAttrValue> getSkuAttrValueList(Long skuId) {
        return null;
    }


    //根据ID获取SkuINfo
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //远程调用 getSkuInfo 抛出异常
        SkuInfo skuInfo = new SkuInfo();

        return skuInfo;
    }

    @Override
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        return null;
    }
}
