package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.PassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录管理
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportController {

    @Autowired
    private PassportService passportService;
    @Autowired
    private RedisTemplate redisTemplate;

    //提交登录
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){
        //1:用户名不能为NULL  密码不能为NULL
        // 就演示一个用户名不为空 其它请自行完成
        if(StringUtils.isEmpty(userInfo.getLoginName())){
            return Result.fail().message("用户名不能为空");
        }
        //2:判断用户名密码是否正确
        userInfo = passportService.login(userInfo);
        if(null != userInfo){
            //登录成功
            Map data = new HashMap<>();
            data.put("nickName",userInfo.getNickName());
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            data.put("token",token);
            //缓存 K：令牌 V：userInfo的用户ID
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    userInfo.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(data);
        }else{
            return Result.fail().message("登录失败");
        }
    }
}
