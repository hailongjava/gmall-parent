package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 订单管理
 */
@Service
public class OrderServiceImpl implements OrderService {

    //物流的网地址
    @Value("${ware.url}")
    private String wareUrl;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    //生成交易号
    @Override
    public String getTradeNo(String userId) {
        //缓存中保存交易号的Key
        String tradeNoKey = "user:" + userId + ":tradeNo";
        //生成交易号
        String tradeNo = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        return tradeNo;
    }

    //判断是否有货 有库存
    @Override
    public boolean hasStock(Long skuId, Integer skuNum) {
        String url = wareUrl + "/hasStock";
        return "1".equals(HttpClientUtil.doGet(url));//Http协议的请求
    }

    //保存订单 订单详情  删除购物车已经提交订单了
    @Override
    public void sumbitOrder(OrderInfo orderInfo) {
        //1:保存订单 订单详情\
        //总金额
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //保存订单
        orderInfoMapper.insert(orderInfo);
        //2:删除购物车已经提交订单了

    }
}
