package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient("service-cart")
public interface CartFeignClient {

    //加入购物车
    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable(name = "skuId") Long skuId,
                              @PathVariable(name = "skuNum") Integer skuNum);

    //查询当前登录的用户的选中了的购物车集合
    @GetMapping("/api/cart/getCartCheckedList")
    public List<CartInfo> getCartCheckedList();
}
