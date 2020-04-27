package com.atguigu.gmall.payment.service;

public interface AlipayService {
    String submit(Long orderId) throws Exception;

    void refund(String out_trade_no);

    void closePayment(String outTradeNo) throws Exception;
}
