package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

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
            //支付状态
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());

            //保存支付信息表
            paymentInfoMapper.insert(paymentInfo);
        }
        //3:已经生成 直接返回
        return paymentInfo;
    }

    @Autowired
    private RabbitService rabbitService;
    //支付成功时 更新支付信息表  班长 幂等性问题   重复性问题
    @Override
    public void paySuccess(Map<String, String> paramMap, String name) {
        //可能出现重复性问题   更新支付信息表为多次  钱有关系的时候
        //1:检查支付信息表  是否已经更新完成
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new
                QueryWrapper<PaymentInfo>().eq("out_trade_no", paramMap.get("out_trade_no")));
        //防止幂等性问题
        if(PaymentStatus.UNPAID.name().equals(paymentInfo.getPaymentStatus())){
            //更新支付信息表
            paymentInfo.setTradeNo(paramMap.get("trade_no"));
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(JSON.toJSONString(paramMap));
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfoMapper.updateById(paymentInfo);
            //同时 更新订单状态
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    MqConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
        }
    }
}
