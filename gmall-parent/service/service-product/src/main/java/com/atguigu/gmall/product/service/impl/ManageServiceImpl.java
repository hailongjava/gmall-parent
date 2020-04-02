package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 后台管理 业务层
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    //获取一级分类
    @Override
    public List<BaseCategory1> getCategory1() {
        //查询所有一级分类
        return baseCategory1Mapper.selectList(null);
    }
    //根据一级分类的ID 获取二级分类的集合
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(
                new QueryWrapper<BaseCategory2>().eq("category1_id",category1Id));
    }
    //根据二级分类的ID 获取三级分类的集合
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(
                new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }
    //根据一二三级分类的ID 查询平台属性（属性值集合）
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        // select * from base_attr_info inner join base_attr_value on ... where () or () or ()
        //Mybatis Plus  支持单表操作
        //解决办法 ： Mapper接口  Mapper.xml
        return baseAttrInfoMapper.attrInfoList(category1Id,category2Id,category3Id);
    }
    //保存平台属性及属性值
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1:保存平台属性 1
        baseAttrInfoMapper.insert(baseAttrInfo);
        //2:保存平台属性值表 多
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(!CollectionUtils.isEmpty(attrValueList)){
            attrValueList.forEach(attrValue -> {
                //平台属性表的ID 作为外键
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(attrValue);
            });
        }

    }
}
