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
    //测试分布式锁
    @GetMapping("testRedisLock")
    public Result testRedisLock(){
        testService.testRedisLock();
        return Result.ok();
    }

    //测试读锁
    @GetMapping("/testReadLock")
    public Result testReadLock(){
        String msg = testService.testReadLock();
        return Result.ok(msg);
    }
    //测试写锁
    @GetMapping("/testWriteLock")
    public Result testWriteLock(){
        testService.testWriteLock();
        return Result.ok();
    }

}
