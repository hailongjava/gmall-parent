package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品详情页面 数据汇总
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    //查询商品详情页面所有数据
    @Override
    public Map getItem(Long skuId) {
        Map result = new HashMap();
        //1:库存数据
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        result.put("skuInfo",skuInfo);
        //2
        //3
        //4
        return result;
    }
}
