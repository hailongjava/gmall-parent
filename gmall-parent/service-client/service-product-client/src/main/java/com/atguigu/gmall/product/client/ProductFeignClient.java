package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品微服务对外暴露的接口
 */
@FeignClient(name = "service-product",fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    //根据skuId 查询库存表
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId);
    //根据三级分类的ID 查询一二三级分类的ID、名称
    @GetMapping("/api/product/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(name = "category3Id") Long category3Id);

    //单独查询价格
    @GetMapping("/api/product/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable(name = "skuId") Long skuId);

    //-- 根据商品ID查询销售属性及销售属性值集合
    //-- 并且根据当前skuId库存ID查询出对应的销售属性值
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable(name = "skuId") Long skuId,
                                                          @PathVariable(name = "spuId") Long spuId);


    //查询组合对应库存ID
    // {颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId}
    @ApiOperation("查询组合对应库存ID ")
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable(name = "spuId") Long spuId);


    //查询分类视图对象集合 （查询全部）
    @GetMapping("/api/product/getBaseCategoryList")
    public List<Map> getBaseCategoryList();
}
