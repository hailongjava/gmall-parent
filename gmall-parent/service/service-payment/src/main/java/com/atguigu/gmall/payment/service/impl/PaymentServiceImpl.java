package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 支付信息表管理
 */
@Service
public class PaymentServiceImpl implements PaymentService {


    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    //保存支付信息数据
    @Override
    public PaymentInfo save(Long orderId, String name) {
        //1:判断当前订单是否已经生居支付信息数据
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(
                new QueryWrapper<PaymentInfo>().eq("order_id", orderId));

        if(null == paymentInfo){
            paymentInfo = new PaymentInfo();
            //2:尚未生成 、生成支付信息数据
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            //对外交易编号
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            //订单ID
            paymentInfo.setOrderId(orderId);
            //支付类型
            paymentInfo.setPaymentType(name);
            //交易编号 是支付成功之后回调数据
            //交付金额
            paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
            //交易内容
            paymentInfo.setSubject(orderInfo.getTradeBody());
            //时间
            paymentInfo.setCreateTime(new Date());

            //保存支付信息表
            paymentInfoMapper.insert(paymentInfo);
        }
        //3:已经生成 直接返回
        return paymentInfo;
    }
}
