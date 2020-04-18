package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo addToCart(Long skuId, Integer skuNum,String userId);

    List<CartInfo> cartList(String userId, String userTempId);

    void checkCart(Long skuId, Integer isChecked);

    List<CartInfo> getCartCheckedList(String userId);
}
