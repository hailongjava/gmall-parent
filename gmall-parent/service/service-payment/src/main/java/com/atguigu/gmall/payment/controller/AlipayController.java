package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.payment.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付宝管理
 */
@RestController
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    //支付宝支付开始
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable(name = "orderId") Long orderId){
        return alipayService.submit(orderId);

        //return '<!DOCTYPE html><head>';
    }
}
