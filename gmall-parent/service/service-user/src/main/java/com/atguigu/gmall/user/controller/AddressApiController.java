package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.AddressService;
import net.bytebuddy.asm.Advice;
import org.apache.tomcat.jni.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 收货地址
 */
@RestController
@RequestMapping("/api/address")
public class AddressApiController {

    @Autowired
    private AddressService addressService;

    //根据当前用户ID查询
    @GetMapping("/auth/findAddressListByUserId")
    public List<UserAddress> findAddressListByUserId(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        return addressService.findAddressListByUserId(userId);
    }
}
