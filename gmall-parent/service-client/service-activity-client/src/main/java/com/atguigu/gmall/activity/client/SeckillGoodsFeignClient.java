package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 远程调用  秒杀接口
 */
@FeignClient("service-activity")
public interface SeckillGoodsFeignClient {


    //查询缓存中所有今天要秒杀的商品
    @GetMapping("/api/activity/seckill/list")
    public List<SeckillGoods> list();

    //查询缓存中今天的一个秒杀商品 显示在详情页面上
    @GetMapping("/api/activity/seckill/detail/{skuId}")
    public SeckillGoods getItemById(@PathVariable(name = "skuId") String skuId);

    //查询秒杀的商品清单(订单详情集合）  总条数 总金额
    @GetMapping("/api/activity/seckill/auth/trade")
    public Map trade();

}
