package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    //本地锁
    @Override
    public synchronized void testLock() {
        //1: num = 0默认是0
        Integer num = (Integer) redisTemplate.opsForValue().get("num");
        if(null != num){
            num++;
            redisTemplate.opsForValue().set("num",num);
        }
    }


    //分布式锁  使用Redis完成的分布式锁
    @Override
    public void testRedisLock() {

        //大量代码 ....  操作Mysql数据库？

        //锁的Value值
        String uuid = UUID.randomUUID().toString();
        //1:上锁   命令：setnx   Java代码 setIfAbsent
        // 命令：setnx 返回值 1   Java代码 1转成true    返回值0 java代码 0转成false
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,
                10, TimeUnit.SECONDS);//自动解锁
        if(lock){
            //获取到锁
            //2: num 追加
            Integer num = (Integer) redisTemplate.opsForValue().get("num");
            if(null != num){
                num++;
                redisTemplate.opsForValue().set("num",num);
            }
            //3:解锁 具备原子性操作
            //LUA脚本  LUA 由C语言 写的脚本  Nginx上运行可以Redis上运行 Nginx与Redis都C语言写的
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), Arrays.asList(uuid));
//            //1)获取此锁的值 判断是否是自己的锁
//            String code = (String) redisTemplate.opsForValue().get("lock");
//            if(!StringUtils.isEmpty(code) && uuid.equals(code)){
//                //2)是自己的锁 删除
//                redisTemplate.delete("lock");//手动解锁
//            }
        }else{
            //未获取到锁  重新调用此方法
            testRedisLock();
        }
    }

    //测试写锁的
    @Override
    public void testWriteLock() {
        //1:上写锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        RLock rLock = lock.writeLock();
        rLock.lock(10,TimeUnit.SECONDS);
        //从Redis缓存中获取信息
        this.redisTemplate.opsForValue().set("msg","测试Redisson的读写锁");
    }

    //测试读锁
    @Override
    public String testReadLock() {
        //1:上读锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        RLock rLock = lock.readLock();
        rLock.lock(10,TimeUnit.SECONDS);
        String msg = (String) redisTemplate.opsForValue().get("msg");
        return msg;
    }

    @Autowired
    private RedissonClient redissonClient;
    //使用Redisson完成分布式锁
    public void testRedissonLock() throws Exception {

        //1:上锁
        RLock lock = redissonClient.getLock("lock");
        ////连接redis服务器进行获取锁   上锁成功：代码继续执行、上锁失败：代码阻塞在此 等待最终获取到锁
        //第一种情况：根据业务场景：决定此次是否必须要获取锁
        lock.lock(10,TimeUnit.SECONDS);//获取到锁之后 10s后自动解锁
        //第二种情况：根据业务要求：不必须获取到锁
        //参数1：尝试1秒之内获取锁 超过10秒就不再获取  如果在10秒之内获取到锁了 返回值 true 反之  false
        // 缓存击穿
        //boolean lock1 = lock.tryLock(10, 10, TimeUnit.SECONDS);

        //2: num 追加
        Integer num = (Integer) redisTemplate.opsForValue().get("num");
        if(null != num){
            num++;
            redisTemplate.opsForValue().set("num",num);
        }
        //3:解锁
        lock.unlock();

    }
}
