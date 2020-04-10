package com.atguigu.gmall.product.service;

public interface TestService {
    void testLock();

    void testRedisLock();

    void testWriteLock();

    String testReadLock();
}
