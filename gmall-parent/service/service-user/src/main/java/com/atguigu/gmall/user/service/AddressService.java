package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

public interface AddressService {
    List<UserAddress> findAddressListByUserId(String userId);
}
