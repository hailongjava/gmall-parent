package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

public interface PaymentService {
    PaymentInfo save(Long orderId, String name);
}
