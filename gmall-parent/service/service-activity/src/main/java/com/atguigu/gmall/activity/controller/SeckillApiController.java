package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀管理
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillApiController {


    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private RedisTemplate redisTemplate;

    //查询缓存中所有今天要秒杀的商品
    @GetMapping("/list")
    public List<SeckillGoods> list() {

        return seckillGoodsService.list();
    }

    //查询缓存中今天的一个秒杀商品 显示在详情页面上
    @GetMapping("/detail/{skuId}")
    public SeckillGoods getItemById(@PathVariable(name = "skuId") String skuId) {
        return seckillGoodsService.getItemById(skuId);
    }

    //'/auth/getSeckillSkuIdStr/' + skuId
    //获取下单码
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable(name = "skuId") String skuId
            , HttpServletRequest request) {

        SeckillGoods seckillGoods = seckillGoodsService.getItemById(skuId);
        //1、如果商品下架了或  审核不通过了  清除缓存中的数据
        if (null == seckillGoods) {
            return Result.fail().message("此商品已下架");
        }
        //2:时间
        //开始时间
        Date startTime = seckillGoods.getStartTime();
        //结束时间
        Date endTime = seckillGoods.getEndTime();

        //1)开始时间大于 当前时间   此活动还未开始
        if (DateUtil.dateCompare(new Date(), startTime)) {
            return Result.fail().message("此活动还未开始");
        }
        //2)当前时间大于 结束时间   此活动已经结束
        if (DateUtil.dateCompare(endTime, new Date())) {
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

    //秒杀商品开始抢购
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable(name = "skuId") String skuId, String skuIdStr, HttpServletRequest
            request) {
//        1、判断下单码是否正确
        if (StringUtils.isEmpty(skuIdStr)) {
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        String userId = AuthContextHolder.getUserId(request);
        String s = MD5.encrypt(userId + skuId);
        if (!skuIdStr.equals(s)) {
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
//        2、状态位  Map map key:skuId  Value:1或是0
        Object o = CacheHelper.get(skuId);
        if(null == o){
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if(o.equals("0")){
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
//        3、发消息（用户的ID、SkuID）
        UserRecode userRecode = new UserRecode();
        userRecode.setUserId(userId);
        userRecode.setSkuId(Long.parseLong(skuId));
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                MqConst.ROUTING_SECKILL_USER,userRecode);
//        4、返回值
        return Result.ok();
    }
    //轮循查询抢购结果
    //间隔3秒执行一次
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable(name = "skuId") String skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
//        1、判断缓存中是否有此用户
        Object isExist = redisTemplate.opsForValue().get(RedisConst.SECKILL_USER +
                userId + skuId);
        if(null != isExist){
    //        2、有   判断缓存中是否有订单
            Object o = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDERS_USERS + userId);
            if(null != o){
                //        --1）有订单  显示 我的订单
                return Result.build(null,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
            }else{
                //       -- 2） 无   判断缓存中是否有抢购资格
                Object or = redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDERS + userId);
                if(null != or){
                //          ----1）有   抢购成功
                    return Result.build(null,ResultCodeEnum.SECKILL_SUCCESS);
                }else{
                //          ----2）没有   抢购失败
                    return Result.build(null,ResultCodeEnum.SECKILL_FAIL);
                }
            }
        }
//        3、没有  返回排队中
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }
    //查询秒杀的商品清单(订单详情集合）  总条数 总金额
    @GetMapping("/auth/trade")
    public Map trade(HttpServletRequest request){
        Map result = new HashMap();

        String userId = AuthContextHolder.getUserId(request);
        //2:商品清单  （订单详情集合）  Redis缓存 秒杀订单资格  有秒杀商品
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //长度1
        OrderDetail oD = new OrderDetail();
        //秒杀商品的信息保存到订单详情对象中
        OrderRecode orderRecode = (OrderRecode) redisTemplate.opsForValue().get(RedisConst.SECKILL_ORDERS + userId);

        if(null != orderRecode){
            SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
            //库存ID
            oD.setSkuId(seckillGoods.getSkuId());
            //图片
            oD.setImgUrl(seckillGoods.getSkuDefaultImg());
            //价格
            oD.setOrderPrice(seckillGoods.getCostPrice());
            //标题
            oD.setSkuName(seckillGoods.getSkuName());
            //数量
            oD.setSkuNum(orderRecode.getNum());
        }

        orderDetailList.add(oD);

        result.put("detailArrayList",orderDetailList);
        //总件数
        long totalNum = orderDetailList.stream().
                collect(Collectors.summarizingInt(OrderDetail::getSkuNum)).getSum();
        //总金额
        double totalAmount = orderDetailList.stream().
                collect(Collectors.summarizingDouble(orderDetail -> {
                    return orderDetail.getOrderPrice().doubleValue()*orderDetail.getSkuNum();
                })).getSum();
        result.put("totalNum",totalNum);
        result.put("totalAmount",totalAmount);
        return result;
    }
    //提交订单
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDERS_USERS + userId,orderInfo,
                30,TimeUnit.MINUTES);
        return Result.ok();
    }
}
