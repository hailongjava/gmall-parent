package com.atguigu.gmall.list.client;


import com.atguigu.gmall.common.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-list")
public interface ListFeignClient {


    //更新当前库存的热度评分
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable(name = "skuId") Long skuId);
}
