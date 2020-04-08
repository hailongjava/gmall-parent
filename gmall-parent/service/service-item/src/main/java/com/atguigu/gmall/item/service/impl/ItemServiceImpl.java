package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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
        //2:根据category3_id 三级分类的Id查询二级分类的ID及名称、一级分类的ID及名称
        BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView",baseCategoryView);
        //3:单独查询价格
        BigDecimal price = productFeignClient.getPrice(skuId);
        result.put("price",price);
        //4:
        // -- 根据商品ID查询销售属性及销售属性值集合
        // -- 并且根据当前skuId库存ID查询出对应的销售属性值
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.
                getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        return result;
    }
}
