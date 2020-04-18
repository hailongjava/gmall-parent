package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 购物车管理
 */
@Controller
public class CartController {

    //远程调用 购物车微服务
    @Autowired
    private CartFeignClient cartFeignClient;

    //加入购物车    保存购物车表  如果存在了 就更新购物车表
    @GetMapping("/addCart.html")
    public String addCart(Long skuId, Integer skuNum, Model model){
        //保存当前库存到购物车中
        CartInfo cartInfo = cartFeignClient.addToCart(skuId,skuNum);
        model.addAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }
    //去购物车结算
    @GetMapping("/cart.html")
    public String cart(){
        return "cart/index";
    }

}
