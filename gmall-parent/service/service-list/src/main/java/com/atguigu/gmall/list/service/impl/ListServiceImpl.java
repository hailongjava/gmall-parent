package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import javafx.stage.Screen;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 搜索管理
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;//ES原生态Api包   查询方法性能非常高
    @Autowired
    private GoodsDao goodsDao;//添加 删除  修改
    @Autowired
    private ProductFeignClient productFeignClient;


    //上架库存到ES索引库
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();

        //1:ID
        goods.setId(skuId);
        //2:标题
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setTitle(skuInfo.getSkuName());
        //3:默认图片
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //4:价格
        goods.setPrice(skuInfo.getPrice().doubleValue());
        //5:当前时间
        goods.setCreateTime(new Date());
        //6:品牌ID
        goods.setTmId(skuInfo.getTmId());
        //7:品牌名称
        BaseTrademark trademarkByTmId = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());
        goods.setTmName(trademarkByTmId.getTmName());
        //8:一二三级分类ID 、名称
        BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(baseCategoryView.getCategory1Id());
        goods.setCategory1Name(baseCategoryView.getCategory1Name());
        goods.setCategory2Id(baseCategoryView.getCategory2Id());
        goods.setCategory2Name(baseCategoryView.getCategory2Name());
        goods.setCategory3Id(baseCategoryView.getCategory3Id());
        goods.setCategory3Name(baseCategoryView.getCategory3Name());

        //9:刚保存的索引库 热度的评分 默认是0
        //10:平台属性集合
        List<SkuAttrValue> skuAttrValueList =
                productFeignClient.getSkuAttrValueList(skuId);
        //Jdk1.8 Lomda表达式
        List<SearchAttr> searchAttrList = skuAttrValueList.stream().map((skuAttrValue) -> {
            SearchAttr searchAttr = new SearchAttr();
            //1:平台属性ID
            searchAttr.setAttrId(skuAttrValue.getBaseAttrInfo().getId());
            //2:平台属性名称
            searchAttr.setAttrName(skuAttrValue.getBaseAttrInfo().getAttrName());
            //3:平台属性值名称
            searchAttr.setAttrValue(skuAttrValue.getBaseAttrValue().getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        //保存索引
        goodsDao.save(goods);

    }

    //下架库存从ES索引库
    @Override
    public void lowerGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }


    @Autowired
    private RedisTemplate redisTemplate;

    //更新当前库存的热度评分
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hotScore";
        //1:先查询Redis缓存  更新Redis缓存
        // zset     小明  语文 100   数学 98  英语 90  本次当前商品热度
        // zset 参数1：热度   参数2：当前库存ID 参数3：增加的分数 返回值：当前库存ID的总分类
        Double score = redisTemplate.opsForZSet().incrementScore(key, skuId, 1);
        //2:判断当前热度评分是否 10 20 30 40 节点 再更新ES
        System.out.println("当前分数：" + score);
        if (score % 10 == 0) {
            System.out.println("更新索引库：" + score);
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(score));//四舍五入
            goodsDao.save(goods);
        }
    }


    //开始搜索
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //1:构建条件对象
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        SearchResponse searchResponse = null;
        try {
            //2:执行查询   入参：搜索请求对象   返回值：搜索响应对象
            searchResponse = restHighLevelClient.
                    search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //3:返回结果
        SearchResponseVo vo = processSearchResponse(searchResponse);
        //当前页
        vo.setPageNo(searchParam.getPageNo());
        //每页数
        vo.setPageSize(searchParam.getPageSize());
        //总页数
        //  （总条数 + 每页数 - 1）/每页数 == 总页数
        Long total = vo.getTotal();
        Integer pageSize = vo.getPageSize();
        vo.setTotalPages ((total + (long)pageSize - 1)/(long)pageSize);
        return vo;
    }

    //处理搜索之后的返回结果
    public SearchResponseVo processSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo vo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        //1;总条数
        long totalHits = hits.totalHits;
        System.out.println("总条数：" + totalHits);
        vo.setTotal(totalHits);
        //2;商品结果集  List<Goods>
        SearchHit[] hits1 = hits.getHits();
        if (null != hits1 && hits1.length > 0) {
            List<Goods> goodsList = Arrays.stream(hits1).map(searchHit -> {
                //商品数据  // 商品数据 Json格式字符串
                return JSON.parseObject(searchHit.getSourceAsString(), Goods.class);
            }).collect(Collectors.toList());
            vo.setGoodsList(goodsList);
        }
        //3:品牌结果集  List<SearchResponseTmVo>
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) searchResponse.getAggregations().asMap().get("tmIdAgg");
        List<SearchResponseTmVo> tmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo tmVo = new SearchResponseTmVo();
            //1:品牌ID
            tmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));
            //2;品牌名称
            //在tmIdAgg 分组中 遍历每个分组里面有二级分组
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            tmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());
        vo.setTrademarkList(tmVoList);
        return vo;
    }

    //构建条件对象并返回
    public SearchRequest buildSearchRequest(SearchParam searchParam) {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //构建条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1：关键词
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有索引库的数据
        //matchQuery : 匹配查询
        // 1）先分词 我是中国人   like %我%  OR  like %是%  OR like %中国 OR like %国人 OR like %中国人%
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            searchSourceBuilder.query(QueryBuilders.matchQuery("title", keyword));
        }
        //2:过滤条件   暂时不查询

        //3:排序   暂时不排序

        //4:分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        // 开始行
        searchSourceBuilder.from((pageNo - 1) * pageSize);
        //每页数
        searchSourceBuilder.size(pageSize);
        //5:高亮  暂时不高亮

        //6:分组查询
        //品牌分组查询    需要起别名： 目地是为了将查询进通过别名获取出来
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                              .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName")));
        //平台属性分组查询  暂时不平台属性分组查询
        //指定查询的索引库
        searchRequest.indices("goods");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
