package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 秒杀管理
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillApiController {


    @Autowired
    private SeckillGoodsService seckillGoodsService;

    //查询缓存中所有今天要秒杀的商品
    @GetMapping("/list")
    public List<SeckillGoods> list(){

        return seckillGoodsService.list();
    }
    //查询缓存中今天的一个秒杀商品 显示在详情页面上
    @GetMapping("/detail/{skuId}")
    public SeckillGoods getItemById(@PathVariable(name = "skuId") String skuId){
        return seckillGoodsService.getItemById(skuId);
    }
    //'/auth/getSeckillSkuIdStr/' + skuId
    //获取下单码
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable(name = "skuId") String skuId
    , HttpServletRequest request){

        SeckillGoods seckillGoods = seckillGoodsService.getItemById(skuId);
        //1、如果商品下架了或  审核不通过了  清除缓存中的数据
        if(null == seckillGoods){
            return Result.fail().message("此商品已下架");
        }
        //2:时间
        //开始时间
        Date startTime = seckillGoods.getStartTime();
        //结束时间
        Date endTime = seckillGoods.getEndTime();

        //1)开始时间大于 当前时间   此活动还未开始
        if(DateUtil.dateCompare(new Date(),startTime)){
            return Result.fail().message("此活动还未开始");
        }
        //2)当前时间大于 结束时间   此活动已经结束
        if(DateUtil.dateCompare(endTime,new Date())){
            return Result.fail().message("此活动已经结束");
        }
        //3) 开始时间 < 当前时间  <结束时间   此商品正在秒杀活动中

        //3:  1）生成下单码    保存下单码   判断下单码是否正确   Redis缓存来实现 （曾经 交易号）
        //    2）生成下单码     不保存下单码  判断下单码是否正确   使用算法
        //   MD5(用户ID + skuId)
        String userId = AuthContextHolder.getUserId(request);
        //下单码
        String skuIdStr = MD5.encrypt(userId + skuId);
        return Result.ok(skuIdStr);
    }
}
