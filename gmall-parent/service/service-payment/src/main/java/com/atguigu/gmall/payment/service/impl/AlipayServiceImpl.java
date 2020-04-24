package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * 支付宝管理
 */
@Service
public class AlipayServiceImpl implements AlipayService {


    //支付宝 微信  公用同一个支付信息表  实现类
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;

    //支付宝支付开始
    @Override
    public String submit(Long orderId) throws Exception{
        //1、保存支付信息数据
        PaymentInfo paymentInfo = paymentService.save(orderId, PaymentType.ALIPAY.name());
        //2、与支付宝进行交互
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //入参
        Map map = new HashMap<>();
        //四个必选
        //第三方交易号
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        //销售产品码
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        //总金额
        //map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("total_amount","0.01");
        //标题
        map.put("subject",paymentInfo.getSubject());
        //2个URL路径
        //同步刷新页面的回调路径
        request.setReturnUrl(AlipayConfig.return_payment_url);
        //异步回调通知商家
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if(response.isSuccess()){
            System.out.println("调用成功");
            return response.getBody();
        } else {
            System.out.println("调用失败");
            return null;
        }
    }

    //退钱
    @Override
    public void refund(String out_trade_no) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map map = new HashMap();
        map.put("refund_amount","0.01");
        map.put("out_trade_no",out_trade_no);

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

    }
}
