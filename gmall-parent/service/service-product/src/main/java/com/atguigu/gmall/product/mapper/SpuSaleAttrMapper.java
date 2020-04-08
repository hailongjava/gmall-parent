package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    //根据spuId  查询销售属性及嵌套的属性值集合
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    //-- 根据商品ID查询销售属性及销售属性值集合
    //-- 并且根据当前skuId库存ID查询出对应的销售属性值
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
}
