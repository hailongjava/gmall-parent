package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车管理
 */
@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private CartInfoMapper cartInfoMapper;

    //加入购物车
    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum,String userId) {
        //1:加入购物车时 当前库存是否在购物车中已经存在了
        //  根据用户ID、SkuID 进行
        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>()
                .eq("user_id", userId)
                .eq("sku_id", skuId));
        //2:存在了  更新此购物车的数量
        if(null != cartInfo){
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //TODO
            cartInfoMapper.updateById(cartInfo);
        }else{
        //3:不存在  添加此购物车到数据库
            cartInfo = new CartInfo();
            //TODO
            cartInfoMapper.insert(cartInfo);
        }
        return cartInfo;
    }
}
