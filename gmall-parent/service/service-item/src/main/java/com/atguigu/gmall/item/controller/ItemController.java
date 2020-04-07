package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品详情页面微服务
 * Swagger
 *   /api/.*
 */
@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    //数据汇总
    @GetMapping("/{skuId}")
    public Map getItem(@PathVariable(name = "skuId") Long skuId){
        return itemService.getItem(skuId);
    }

}
