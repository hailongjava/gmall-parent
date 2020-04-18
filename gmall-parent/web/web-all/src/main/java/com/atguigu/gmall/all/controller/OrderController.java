package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.address.client.AddressFeignClient;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理
 * 结算
 * 提交订单
 */
@Controller
public class OrderController {

    @Autowired
    private AddressFeignClient addressFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    //结算
    @GetMapping("/trade.html")
    public String trade(Model model){

        //1:收货地址集合    用户微服务
        List<UserAddress> addressList = addressFeignClient.findAddressListByUserId();
        model.addAttribute("userAddressList",addressList);
        //2:商品清单  （订单详情集合）  来源于购物车   购物车微服务
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList();



        //List<OrderDetail>
        List<OrderDetail> orderDetailList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            //库存ID
            orderDetail.setSkuId(cartInfo.getSkuId());
            //图片
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            //价格
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            //标题
            orderDetail.setSkuName(cartInfo.getSkuName());
            //数量
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());

        model.addAttribute("detailArrayList",orderDetailList);

        //总件数
        long totalNum = orderDetailList.stream().
                collect(Collectors.summarizingInt(OrderDetail::getSkuNum)).getSum();
        //总金额
        double totalAmount = orderDetailList.stream().
                collect(Collectors.summarizingDouble(orderDetail -> {
                    return orderDetail.getOrderPrice().doubleValue()*orderDetail.getSkuNum();
                })).getSum();
        model.addAttribute("totalNum",totalNum);
        model.addAttribute("totalAmount",totalAmount);

        return "order/trade";
    }
}
