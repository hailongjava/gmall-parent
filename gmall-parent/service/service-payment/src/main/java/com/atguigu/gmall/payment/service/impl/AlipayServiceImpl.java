package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝管理
 */
@Service
public class AlipayServiceImpl implements AlipayService {


    //支付宝 微信  公用同一个支付信息表  实现类
    @Autowired
    private PaymentService paymentService;

    //支付宝支付开始
    @Override
    public String submit(Long orderId) {
        //1、保存支付信息数据
        PaymentInfo paymentInfo = paymentService.save(orderId, PaymentType.ALIPAY.name());
        //2、与支付宝进行交互

        return null;
    }
}
