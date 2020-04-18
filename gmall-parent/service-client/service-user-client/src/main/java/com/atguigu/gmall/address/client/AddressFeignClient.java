package com.atguigu.gmall.address.client;


import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("service-user")
public interface AddressFeignClient {


    //根据当前用户ID查询
    @GetMapping("/api/address/auth/findAddressListByUserId")
    public List<UserAddress> findAddressListByUserId();
}
