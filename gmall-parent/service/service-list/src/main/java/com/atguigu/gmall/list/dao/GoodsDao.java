package com.atguigu.gmall.list.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Mybatis-Plus
 */
public interface GoodsDao extends ElasticsearchRepository<Goods,Long> {

}
