package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理
 */
@Api(tags = "后台管理")
@RestController
@RequestMapping("/admin/product") // 所有请求的路径都是/admin/product开始的
public class ManageController {

    @Autowired
    private ManageService manageService;

    //获取一级分类
    @ApiOperation("获取一级分类")
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        //获取一级分类
        List<BaseCategory1> baseCategory1List = manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

}
