package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
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
        goods.setTmLogoUrl(trademarkByTmId.getLogoUrl());
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
                Goods goods = JSONObject.parseObject(searchHit.getSourceAsString(), Goods.class);
                //判断是否有高亮  title
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if(null != highlightFields && highlightFields.size() > 0){
                    HighlightField title = highlightFields.get("title");
                    String titleHighlight = title.fragments()[0].toString();
                    goods.setTitle(titleHighlight);
                }
                return goods;
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

            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            tmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());
        vo.setTrademarkList(tmVoList);
        //4:平台属性集合结果解析  private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        ParsedNested attrsAgg = (ParsedNested) searchResponse.getAggregations().asMap().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
            //1:平台属性ID
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //2;平台属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //3:平台属性值集合
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> bucketValueList = attrValueAgg.getBuckets().stream().
                    map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValueList(bucketValueList);
            return attrVo;
        }).collect(Collectors.toList());
        //设置平台属性解析结果
        vo.setAttrsList(attrsVoList);
        return vo;
    }

    //构建条件对象并返回
    public SearchRequest buildSearchRequest(SearchParam searchParam) {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //构建条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //组合条件对象（多条件组合 ）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1：关键词
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有索引库的数据
        //matchQuery : 匹配查询
        // 1）先分词 我是中国人   like %我%  OR  like %是%  OR like %中国 OR like %国人 OR like %中国人%
        //  苹果手机   like %苹果&  and like %手机&   并集 应该取交集
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword)
                    .operator(Operator.AND));
        }
        //2:过滤条件
        // 品牌  、  品牌的ID、名称   页面传递过来的ID、名称都是字符串类型  接收的时候 Long Springmvc 转换的
        //  品牌ID:品牌名称
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            String[] t = StringUtils.split(trademark, ":");
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",t[0]));
        }
        // 一二三级分类ID 、
        Long category1Id = searchParam.getCategory1Id();
        if(null != category1Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",category3Id));
        }
        // 平台属性
        String[] props = searchParam.getProps();
        if(null != props && props.length > 0){
            //有平台属性进行过滤
            for (String prop : props) {//5次
                // 平台属性ID:平台属性值名称:平台属性名称
                String[] p = prop.split(":");//3
                //子组合对象
                BoolQueryBuilder subQueryBuilder = QueryBuilders.boolQuery();
                //平台属性ID
                // nestedQuery:嵌套条件对象nestedQuery  参数1：路径 参数2：精准查询 参数3：计算模式
                //   if( null != haha && haha.size > 0)   where and  or  not
                subQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",p[0]));
                //平台属性值名称
                subQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",p[1]));

                //外面父组合对象 追加多个子组合对象
                boolQueryBuilder.filter(QueryBuilders.
                        nestedQuery("attrs",subQueryBuilder,ScoreMode.None));
            }
        }

        //设置组合对象
        searchSourceBuilder.query(boolQueryBuilder);
        //3:排序   K:V   K： 1 2 3  4    1：综合  2：价格 3：新品 ||||   V：desc 或 asc
        // 综合排序： 默认是热度评分
        //
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            String[] o = StringUtils.split(order, ":");//2
            //排序字段
            String orderSort = "";
            switch (o[0]){
                case "1": orderSort = "hotScore"; break;
                case "2": orderSort = "price"; break;
            }
            searchSourceBuilder.sort(orderSort,
                    o[1].equalsIgnoreCase("asc") ? SortOrder.ASC:SortOrder.DESC);

        }else{
            //默认按照  热度评分 由高到低排序
            searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        }

        //4:分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        // 开始行
        searchSourceBuilder.from((pageNo - 1) * pageSize);
        //每页数
        searchSourceBuilder.size(pageSize);
        //5:高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮的字段  ES 字段 == 域
        highlightBuilder.field("title");
        //前缀
        highlightBuilder.preTags("<font color='red'>");
        //后缀
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //6:分组查询  List<对象> tmIds  对象 （tmId  tmName List<平台属性值》)
        //品牌分组查询    需要起别名： 目地是为了将查询进通过别名获取出来
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                              .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                              .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        //平台属性分组查询  nested：嵌套分组  attrs:[ attrId:1
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("attrsAgg","attrs")
                   .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                   .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                   .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //指定查询的索引库
        searchRequest.indices("goods");
        searchRequest.source(searchSourceBuilder);
        searchRequest.types("info");//6.8.1 版本
        return searchRequest;
    }
}
