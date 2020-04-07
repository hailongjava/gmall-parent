package com.atguigu.gmall.product.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  对外暴露的接口 （对接的入口）  前台 商品详情页面  搜索页面  首页 ...
 *  /api/product
 *  Swagger测试：
 *   /admin/.*   后台接口测试
 *   /api/.*     前台接口测试
 */
@Api(tags = "对外暴露的前台接口")
@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    //建议使用原来Service
    @Autowired
    private ManageService manageService;

    //根据skuId 查询库存表
    @ApiOperation("根据skuId查询库存表")
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId){
        return manageService.getSkuInfo(skuId);
    }


}
