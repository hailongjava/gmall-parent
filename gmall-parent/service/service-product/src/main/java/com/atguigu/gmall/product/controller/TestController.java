package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 本地锁  局限性  问题
 */
@RestController
@RequestMapping("/admin/product/test")
public class TestController {


    @Autowired
    private TestService testService;
    //测试本地锁
    @GetMapping("testLock")
    public Result testLock(){
        testService.testLock();
        return Result.ok();
    }


}
