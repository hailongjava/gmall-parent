package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //根据skuId 查询 平台属性ID、属性名称、及平台属性值
    List<SkuAttrValue> getSkuAttrValueList(Long skuId);
}
