package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.SeckillGoodsFeignClient;
import com.atguigu.gmall.address.client.AddressFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运营页面管理
 */
@Controller
public class ActivityController {

    @Autowired
    private SeckillGoodsFeignClient seckillGoodsFeignClient;
    @Autowired
    private AddressFeignClient addressFeignClient;

    //秒杀列表页面
    @GetMapping("/index.html")
    public String index(Model model){
        List<SeckillGoods> list = seckillGoodsFeignClient.list();
        model.addAttribute("list",list);
        return "seckill/index";
    }
    //秒杀详情页面
    @GetMapping("/detail/{skuId}")
    public String detail(@PathVariable(name = "skuId") String skuId,Model model){
        SeckillGoods item = seckillGoodsFeignClient.getItemById(skuId);
        model.addAttribute("item",item);
        return "seckill/item";
    }
    //秒杀抢购页面
    @GetMapping("/seckill/queue.html")
    public String queue(String skuId,String skuIdStr,Model model){
        model.addAttribute("skuId",skuId);
        model.addAttribute("skuIdStr",skuIdStr);
        return "seckill/queue";
    }
    //秒杀商品去下单
    @GetMapping("/seckill/trade.html")
    public String trade(Model model){
        //1:收货地址集合    用户微服务
        List<UserAddress> addressList = addressFeignClient.findAddressListByUserId();
        model.addAttribute("userAddressList",addressList);
        //2:查询秒杀的商品清单(订单详情集合）  总条数 总金额
        Map trade = seckillGoodsFeignClient.trade();
        model.addAllAttributes(trade);
        return "seckill/trade";
    }
}
