package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 加载商品详情页面的所有数据 并渲染商品详情页面 进行响应
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    //进入商品详情页面中
    @GetMapping("/{skuId}.html")
    public String toItemDetail(@PathVariable(name = "skuId") Long skuId, Model model){
        //远程调用商品详情页面所需要的所有数据汇总
        Map result = itemFeignClient.getItem(skuId);//将此Map中的数据保存到Request域中
        model.addAllAttributes(result);//遍历：K V request.setAttribute(k,v）
        return "item/index";
    }


}
