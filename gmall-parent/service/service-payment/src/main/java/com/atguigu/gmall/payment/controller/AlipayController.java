package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝管理
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RabbitService rabbitService;

    //支付宝支付开始
    @GetMapping("/submit/{orderId}")
    @ResponseBody
    public String submit(@PathVariable(name = "orderId") Long orderId) throws Exception{
        return alipayService.submit(orderId);

        //return '<!DOCTYPE html><head>';
    }

    //统一收单交易接口 回显二维码  （用户手机扫描二维码并将金额支付之后）跳转到如下路径
    @GetMapping("/callback/return")
    public String callbackReturn(){

        return "redirect:" + AlipayConfig.return_order_url;
    }

    //异步回调通知   json格式字符串  /callback/notify?param={k:v,k:v}
    @PostMapping("/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String,String> paramMap){//@RequestBody
        //1:验证回调信息的真伪   私钥进行签名  使用公钥进行认证

        try {
            boolean rsaCheckV1 = AlipaySignature.rsaCheckV1(paramMap,
                    AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
            //判断
            if(rsaCheckV1){
                //成功
                System.out.println(JSON.toJSON(paramMap));

               // {"gmt_create":"2020-04-24 14:17:13",//扫描二维码的时间
                // "charset":"utf-8","
                // gmt_payment":"2020-04-24 14:17:17",//成功支付的时间
                // "notify_time":"2020-04-24 14:17:17",
                // "subject":"尚品汇的商品",
                // "buyer_id":"2088502880278392",
                // "invoice_amount":"0.01",
                // "version":"1.0",
                // "notify_id":"2020042400222141717078391415483709",
                // "fund_bill_list":"[{\"amount\":\"0.01\",\"fundChannel\":\"PCREDIT\"}]",
                // "notify_type":"trade_status_sync",
                // "out_trade_no":"ATGUIGU1587709057428754",
                // "total_amount":"0.01",
                // "trade_status":"TRADE_SUCCESS",
                // "trade_no":"2020042422001478391442058098", //支付宝号
                // "auth_app_id":"2018020102122556",
                // "receipt_amount":"0.01",
                // "point_amount":"0.00",
                // "buyer_pay_amount":"0.01",
                // "app_id":"2018020102122556",
                // "seller_id":"2088921750292524"}

//                Map map = new HashMap();
//                map.put("trade_no",paramMap.get(""));
                if("TRADE_SUCCESS".equals(paramMap.get("trade_status"))){
                    //更新支付信息表  发消息RabbitMQ
                    //rabbitService.sendMessage("交换机","routingKey",paramMap);
                    paymentService.paySuccess(paramMap, PaymentType.ALIPAY.name());
                    //通知电商
                }
            }else{
                //失败
                return "failure";
            }
        } catch (AlipayApiException e) {
            // e.printStackTrace();
            return "failure";
        }
        return "success";
    }

    //退钱
    @GetMapping("/refund/{out_trade_no}")
    @ResponseBody
    public Result refund(@PathVariable(name = "out_trade_no") String out_trade_no){
        //退钱
        alipayService.refund(out_trade_no);

        return Result.ok();
    }

}
