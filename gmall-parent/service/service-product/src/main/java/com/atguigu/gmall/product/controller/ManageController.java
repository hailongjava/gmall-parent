package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理
 */
@Api(tags = "后台管理")
@RestController
@RequestMapping("/admin/product") // 所有请求的路径都是/admin/product开始的
//@CrossOrigin
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
    //根据一级分类的ID 获取二级分类的集合
    @ApiOperation("根据一级分类的ID 获取二级分类的集合")
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable(name = "category1Id") Long category1Id){
        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }
    //根据二级分类的ID 获取三级分类的集合
    @ApiOperation("根据二级分类的ID 获取三级分类的集合")
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable(name = "category2Id") Long category2Id){
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }
    //根据一二三级分类的ID 查询平台属性（属性值集合）
    @ApiOperation("根据一二三级分类的ID 查询平台属性（属性值集合） ")
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(
            @PathVariable(name = "category1Id") Long category1Id,
            @PathVariable(name = "category2Id") Long category2Id,
            @PathVariable(name = "category3Id") Long category3Id
    ){
        List<BaseAttrInfo> baseAttrInfoList = manageService.attrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfoList);
    }
    //保存平台属性及属性值
    @ApiOperation("保存平台属性及属性值 ")
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        //保存二张表  平台属性表  属性值表
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }
}
