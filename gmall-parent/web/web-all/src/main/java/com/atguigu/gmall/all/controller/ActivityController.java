package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.SeckillGoodsFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 运营页面管理
 */
@Controller
public class ActivityController {

    @Autowired
    private SeckillGoodsFeignClient seckillGoodsFeignClient;
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
}
