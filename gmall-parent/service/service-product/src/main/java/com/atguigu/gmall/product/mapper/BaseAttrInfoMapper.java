package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    //1:注解的方式  jdbc  存在的问题  Sql与Java代码硬编码问题   Sql语句Mapper文件
    //@Select("select * from .....")
    List<BaseAttrInfo> attrInfoList(@Param("category1Id") Long category1Id,
                                    @Param("category2Id") Long category2Id,
                                    @Param("category3Id") Long category3Id);
    //2:Mapper的配置文件  要求：必须同包且同名
}
