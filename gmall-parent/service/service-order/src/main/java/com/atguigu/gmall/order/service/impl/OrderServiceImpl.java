package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CartInfoMapper cartInfoMapper;
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
        String url = wareUrl + "/hasStock?skuId="+skuId+"&num="+skuNum;
        return "1".equals(HttpClientUtil.doGet(url));//Http协议的请求
    }

    //保存订单 订单详情  删除购物车已经提交订单了
    @Override
    @Transactional
    public Long sumbitOrder(OrderInfo orderInfo) {
        //1:保存订单 订单详情\
        //总金额
        double totalAmount = 0;
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //创建时间
        orderInfo.setCreateTime(new Date());
        //进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //  订单交易编号（第三方支付用)  支付宝或微信使用
        String out_trade_no = "ATGUIGU" + System.currentTimeMillis();
        Random random = new Random();
        for (int i=0;i<3;i++){
            out_trade_no += random.nextInt(10);
        }
        orderInfo.setOutTradeNo(out_trade_no);
       //手机显示名称  为了什么商品进行付款  订单描述 作为付款的标题
        orderInfo.setTradeBody("尚品汇的商品");

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount += orderDetail.getSkuNum()*orderDetail.getOrderPrice().doubleValue();
        }
        //总金额
        orderInfo.setTotalAmount(new BigDecimal(totalAmount));
        //保存订单
        orderInfoMapper.insert(orderInfo);
        //保存订单详情
        orderDetailList.forEach(orderDetail -> {
            //外键
            orderDetail.setOrderId(orderInfo.getId());
            //防止页面篡改价格  查询价格
            BigDecimal price = productFeignClient.getPrice(orderDetail.getSkuId());
            orderDetail.setOrderPrice(price);
            orderDetailMapper.insert(orderDetail);
        });

        //2:删除购物车中已经提交订单的商品
//        Map<String, Object> columnMap = new HashMap<>();
//        columnMap.put("user_id",orderInfo.getUserId());
//        columnMap.put("is_checked",1);
//        cartInfoMapper.deleteByMap(columnMap);

        //TODO 第五步：   2个小时 以后用户没有付钱  此订单取消
        //TODO 此2小时从提交订单开始计时  RabbitMQ高级知识
        return orderInfo.getId();
    }
}
