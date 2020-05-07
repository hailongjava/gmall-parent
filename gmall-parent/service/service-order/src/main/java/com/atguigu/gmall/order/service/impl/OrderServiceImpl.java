package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单管理
 */
@Service
public class OrderServiceImpl implements OrderService {

    //物流的网地址
    @Value("${ware.url}")
    private String wareUrl;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private RabbitService rabbitService;

    //生成交易号
    @Override
    public String getTradeNo(String userId) {
        //缓存中保存交易号的Key
        String tradeNoKey = "user:" + userId + ":tradeNo";
        //生成交易号
        String tradeNo = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    //判断是否有货 有库存
    @Override
    public boolean hasStock(Long skuId, Integer skuNum) {
        String url = wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum;
        return "1".equals(HttpClientUtil.doGet(url));//Http协议的请求
    }

    //保存订单 订单详情  删除购物车已经提交订单了
    @Override
    @Transactional
    public Long sumbitOrder(OrderInfo orderInfo) {
        //1:保存订单 订单详情\
        //总金额
        double totalAmount = 0;
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //创建时间
        orderInfo.setCreateTime(new Date());
        //进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //  订单交易编号（第三方支付用)  支付宝或微信使用
        String out_trade_no = "ATGUIGU" + System.currentTimeMillis();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            out_trade_no += random.nextInt(10);
        }
        orderInfo.setOutTradeNo(out_trade_no);
        //手机显示名称  为了什么商品进行付款  订单描述 作为付款的标题
        orderInfo.setTradeBody("尚品汇的商品");

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount += orderDetail.getSkuNum() * orderDetail.getOrderPrice().doubleValue();
        }
        //总金额
        orderInfo.setTotalAmount(new BigDecimal(totalAmount));
        //保存订单
        orderInfoMapper.insert(orderInfo);
        //保存订单详情
        orderDetailList.forEach(orderDetail -> {
            //外键
            orderDetail.setOrderId(orderInfo.getId());
            //防止页面篡改价格  查询价格
            BigDecimal price = productFeignClient.getPrice(orderDetail.getSkuId());
            orderDetail.setOrderPrice(price);
            orderDetailMapper.insert(orderDetail);
        });

        //2:删除购物车中已经提交订单的商品
//        Map<String, Object> columnMap = new HashMap<>();
//        columnMap.put("user_id",orderInfo.getUserId());
//        columnMap.put("is_checked",1);
//        cartInfoMapper.deleteByMap(columnMap);

        // 第五步：   2个小时 以后用户没有付钱  此订单取消
        // 此2小时从提交订单开始计时  RabbitMQ高级知识  闹钟
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL
                , MqConst.ROUTING_ORDER_CANCEL, orderInfo.getId(), MqConst.DELAY_TIME);

        return orderInfo.getId();
    }


    //延迟消息 来完成订单的取消
    @Override
    public void cancelOrder(Long orderId) {
        //1:判断订单未付钱    实际情况是：2个小时之后 订单仍然未支付  就要取消订单
        //本次为了快速测试  延迟时间是1分钟
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //判断订单的状态是否为未支付
        if (OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())) {
            //取消订单
            //更新订单的状态为关闭
            orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
            //更新订单的进度状态为关闭
            orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());
            //更新订单的失效时间
            orderInfo.setExpireTime(new Date());
            //更新订单
            orderInfoMapper.updateById(orderInfo);


        }

    }

    //根据订单ID查询订单信息
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return orderInfoMapper.selectById(orderId);
    }


    //更新订单  幂等性问题
    @Override
    public void updateOrder(Long orderId) {
        //订单是未支付情况下 更新订单的状态为已支付
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())) {
            orderInfo.setOrderStatus(OrderStatus.PAID.name());
            orderInfo.setProcessStatus(ProcessStatus.PAID.name());
            orderInfoMapper.updateById(orderInfo);
        }
    }

    public void updateOrder(Long orderId, ProcessStatus processStatus) {
        //订单是未支付情况下 更新订单的状态为已支付
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }


    //拆单
    @Override
    public List<OrderInfo> orderSplit(Long orderId, String wareSkuMap) {

        //1:原始订单
        OrderInfo orderInfoOrigin = this.getOrderInfo(orderId);

        // [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);

        List<OrderInfo> orderInfoList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(maps)) {
            for (Map map : maps) {

                String wareId = (String) map.get("wareId");

                OrderInfo subOrderInfo = new OrderInfo();
                //子订单   （从原始订单中复制一份过来
                BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
                //保存时 防止主键冲突
                subOrderInfo.setId(null);
                //仓库ID
                subOrderInfo.setWareId(wareId);
                //设置父Id
                subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
                //库存IDs
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();

                //
                List<OrderDetail> orderDetails = new ArrayList<>();

                List<String> skuIds = (List<String>) map.get("skuIds");
                if (!CollectionUtils.isEmpty(orderDetailList)) {
                    for (OrderDetail orderDetail : orderDetailList) {
                        for (String skuId : skuIds) {
                            if (orderDetail.getSkuId().toString().equals(skuId)) {
                                orderDetails.add(orderDetail);
                            }
                        }
                    }
                }
                subOrderInfo.setOrderDetailList(orderDetails);
                subOrderInfo.sumTotalAmount();
                //保存子订单
                // 保存子订单
                saveOrderInfo(subOrderInfo);

                orderInfoList.add(subOrderInfo);
            }
        }
        updateOrder(orderInfoOrigin.getId(),ProcessStatus.SPLIT);
        return orderInfoList;
    }

    private void saveOrderInfo(OrderInfo subOrderInfo) {
        orderInfoMapper.insert(subOrderInfo);
        List<OrderDetail> orderDetailList = subOrderInfo.getOrderDetailList();
        orderDetailList.forEach(orderDetail -> {
            orderDetail.setOrderId(subOrderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        });
    }


    private OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.
                selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;

    }

    //发消息 通知仓库
    @Override
    public void sendOrderStatus(Long orderId) {
        //更新订单状态
        this.updateOrder(orderId, ProcessStatus.NOTIFIED_WARE);
        //准备json
        //将Map转成Json字符串
        String wareJson = initWareOrder(orderId);
        System.out.println(wareJson);
        //发消息通知仓库
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK, wareJson);

    }

    //准备json
    public String initWareOrder(Long orderId) {
        //1 :查询订单  及订单详情
        OrderInfo orderInfo = getOrderInfo(orderId);

        Map<String, Object> map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    public Map<String, Object> initWareOrder(OrderInfo orderInfo) {
        //2:准备仓库需要的Json数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());// 仓库Id ，减库存拆单时需要使用！
    /*
    details:[{skuId:101,skuNum:1,skuName:
    ’小米手64G’},
    {skuId:201,skuNum:1,skuName:’索尼耳机’}]
     */
        ArrayList<Map> mapArrayList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId", orderDetail.getSkuId());
            orderDetailMap.put("skuNum", orderDetail.getSkuNum());
            orderDetailMap.put("skuName", orderDetail.getSkuName());
            mapArrayList.add(orderDetailMap);
        }
        map.put("details", mapArrayList);
        return map;
    }
}
