package com.atguigu.gmall.user.service;


import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 *
 * 登录管理
 */
@Service
public class PassportServiceImpl implements PassportService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    //判断用户名密码是否正确
    @Override
    public UserInfo login(UserInfo userInfo) {
        //密码被加密了
        userInfo.setPasswd(DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes()));
        //根据用户名 密码 去查询用户
        userInfo = userInfoMapper.selectOne(new QueryWrapper<UserInfo>()
                .eq("login_name", userInfo.getLoginName())
                .eq("passwd", userInfo.getPasswd()));
        //NULL  登录失败
        //有值  登录成功
        return userInfo;
    }

    public static void main(String[] args) {
        String result = DigestUtils.md5DigestAsHex("123".getBytes());
    }
}
