package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 购物车管理
 */
@Controller
public class CartController {

    //远程调用 购物车微服务

    //加入购物车
    @GetMapping("/addCart.html")
    public String addCart(Long skuId,Integer skuNum){
        //保存当前库存到购物车中
        //CartInfo cartInfo =

        return "cart/addCart";
    }

}
