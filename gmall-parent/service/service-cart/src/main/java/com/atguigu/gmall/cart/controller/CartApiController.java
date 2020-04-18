package com.atguigu.gmall.cart.controller;


import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    //加入购物车
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable(name = "skuId") Long skuId,
                              @PathVariable(name = "skuNum") Integer skuNum, HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        return cartService.addToCart(skuId,skuNum,userId);
    }
    //查询购物车集合
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request){
        //真实用户ID
        String userId = AuthContextHolder.getUserId(request);
        //临时用户ID
        String userTempId = AuthContextHolder.getUserTempId(request);
        //去查询购物车集合
        List<CartInfo> cartList = cartService.cartList(userId,userTempId);
        return Result.ok(cartList);
    }
    //选中或取消购物车
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable(name = "skuId") Long skuId,@PathVariable(name = "isChecked")
                            Integer isChecked){

        cartService.checkCart(skuId,isChecked);
        return Result.ok();
    }
    //查询当前登录的用户的选中了的购物车集合
    @GetMapping("/getCartCheckedList")
    public List<CartInfo> getCartCheckedList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        return cartService.getCartCheckedList(userId);
    }

}
