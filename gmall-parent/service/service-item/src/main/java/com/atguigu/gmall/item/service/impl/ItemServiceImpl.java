package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 商品详情页面 数据汇总
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    //查询商品详情页面所有数据
    @Override
    public Map getItem(Long skuId) {
        Map result = new HashMap();
        //1:库存数据
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        //2:根据category3_id 三级分类的Id查询二级分类的ID及名称、一级分类的ID及名称
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", baseCategoryView);
        }, threadPoolExecutor);
        //3:单独查询价格
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getPrice(skuId);
            result.put("price", price);
        }, threadPoolExecutor);
        //4:
        // -- 根据商品ID查询销售属性及销售属性值集合
        // -- 并且根据当前skuId库存ID查询出对应的销售属性值
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.
                    getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }), threadPoolExecutor);

        //5:查询组合数据对应库存ID
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
            Map map = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            result.put("valuesSkuJson", JSON.toJSONString(map));
        }), threadPoolExecutor);
        //主线程已经执行完了   return result Map 必须等全部
        CompletableFuture.allOf(skuInfoCompletableFuture,categoryViewCompletableFuture,
                priceCompletableFuture,spuSaleAttrListCompletableFuture,valuesSkuJsonCompletableFuture).join();
        return result;
    }
}
