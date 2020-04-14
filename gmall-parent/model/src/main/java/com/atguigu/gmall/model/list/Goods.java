package com.atguigu.gmall.model.list;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 *  映射对象   映射到ES索引库    Lucene实现在  Document文档类型  数据类型
 *  POJO  JavaBean
 *  Mysql对比
 *       库           表
 *      indexName   type
 *      shards : 分片指的是：分成三组 索引库的数据过多时  扩容
 *      replicas： 2  2份  2份身处不同服务器 防止某一台服务宕机就索引数据丢失
 *      ES集群搭建之后  ： 由Java代码来控制分片、副本数
 *      自由裂变
 *
 *
 */
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
@Data
public class Goods {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword, index = false)
    private String defaultImg;//type=FieldType 二种 分

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Date)
    private Date createTime; // 新品

    @Field(type = FieldType.Long)
    private Long tmId;

    @Field(type = FieldType.Keyword)
    private String tmName;

    @Field(type = FieldType.Long)
    private Long category1Id;

    @Field(type = FieldType.Keyword)
    private String category1Name;

    @Field(type = FieldType.Long)
    private Long category2Id;

    @Field(type = FieldType.Keyword)
    private String category2Name;

    @Field(type = FieldType.Long)
    private Long category3Id;

    @Field(type = FieldType.Keyword)
    private String category3Name;

    @Field(type = FieldType.Long)
    private Long hotScore = 0L;

    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;//平台属性集合

}
