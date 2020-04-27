package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    PaymentInfo save(Long orderId, String name);

    void paySuccess(Map<String, String> paramMap, String name);

    void closePayment(Long orderId) throws Exception;
}
