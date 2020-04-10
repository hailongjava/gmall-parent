package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public synchronized void testLock() {
        //1: num = 0默认是0
        Integer num = (Integer) redisTemplate.opsForValue().get("num");
        if(null != num){
            num++;
            redisTemplate.opsForValue().set("num",num);
        }
    }
}
