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

}
