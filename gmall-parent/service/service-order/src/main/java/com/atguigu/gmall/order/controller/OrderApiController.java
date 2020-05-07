package com.atguigu.gmall.order.controller;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/api/order")
public class OrderApiController {


    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;

    //拆单
    @PostMapping("/orderSplit")
    public String orderSplit(Long orderId,String wareSkuMap){
        List<OrderInfo> orderInfoList = orderService.orderSplit(orderId, wareSkuMap);

        List<Map> mapArrayList  = new ArrayList<>();
        for (OrderInfo orderInfo : orderInfoList) {
            Map<String, Object> map = orderService.initWareOrder(orderInfo);
            mapArrayList.add(map);
        }
        return JSON.toJSONString(mapArrayList);
    }

    //
    //发消息 通知仓库
    @ApiOperation("发消息通知仓库入参为OrderID")
    @GetMapping("/sendOrderStatus")
    public Result sendOrderStatus(Long orderId){
        orderService.sendOrderStatus(orderId);
        return Result.ok("发消息通知仓库");
    }


    //根据订单ID查询订单信息
    @GetMapping("/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfoById(@PathVariable(name = "orderId") Long orderId){
        return orderService.getOrderInfoById(orderId);
    }

    //交易号
    @GetMapping("/inner/getTradeNo")
    public String getTradeNo(HttpServletRequest request){
        //用户ID
        String userId = AuthContextHolder.getUserId(request);
        return orderService.getTradeNo(userId);

    }
    //提交订单
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(String tradeNo, @RequestBody OrderInfo orderInfo,HttpServletRequest request){
        //防止 二次提交订单
        //1:判断交易号
        if(StringUtils.isEmpty(tradeNo)){
            return Result.fail().message("不能重复提交");
        }
        //用户ID
        String userId = AuthContextHolder.getUserId(request);
        String tradeNoKey = "user:" + userId + ":tradeNo";
        String o = (String) redisTemplate.opsForValue().get(tradeNoKey);
        if(StringUtils.isEmpty(o)){
            return Result.fail().message("不能重复提交");
        }
        //判断交易号
        if(!tradeNo.equals(o)){
            return Result.fail().message("不能重复提交");
        }
        //是第一次提交订单 删除交易号
        redisTemplate.delete(tradeNoKey);
        //2：判断库存  远程调用仓库或物流微服务 在业务层 返回值 0:false 1:true
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //判断当前商品是否有货
            boolean hasStock =  orderService.hasStock(
                    orderDetail.getSkuId(),orderDetail.getSkuNum());
            if(!hasStock){
                //无货
                return Result.fail().message(orderDetail.getSkuName() + ":库存不足");
            }
        }
        //3;保存订单 订单详情
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderService.sumbitOrder(orderInfo);
        return Result.ok(orderId);
    }
    @Autowired
    RabbitService rabbitService;
    @GetMapping("/haha")
    public void haha(){

        rabbitService.sendMessage("exchange.1","","hahaee");
    }


}
