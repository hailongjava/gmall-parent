package com.atguigu.gmall.common.constant;

/**
 * RabbitMQ  常量类
 */
public class MqConst {

    /**
     * 商品上下架
     */
    public static final String EXCHANGE_DIRECT_GOODS = "exchange.direct.goods";
    public static final String ROUTING_GOODS_UPPER = "goods.upper";
    public static final String ROUTING_GOODS_LOWER = "goods.lower";
    //队列
    public static final String QUEUE_GOODS_UPPER  = "queue.goods.upper";
    public static final String QUEUE_GOODS_LOWER  = "queue.goods.lower";


    /**
     * 取消订单，发送延迟队列
     */
    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.direct.order.cancel";//"exchange.direct.order.create" test_exchange;
    public static final String ROUTING_ORDER_CANCEL = "routing.order.cancel";
    //延迟取消订单队列
    public static final String QUEUE_ORDER_CANCEL  = "queue.order.cancel";
    //取消订单 延迟时间 单位：毫秒   现在是1分钟
    public static final int DELAY_TIME  = 1*60*1000;

}
