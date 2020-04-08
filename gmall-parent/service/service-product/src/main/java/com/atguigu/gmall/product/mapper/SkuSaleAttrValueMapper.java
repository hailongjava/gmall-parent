package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    //查询组合对应库存ID
    // {颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId}
    List<Map> getSkuValueIdsMap(Long spuId);
}
