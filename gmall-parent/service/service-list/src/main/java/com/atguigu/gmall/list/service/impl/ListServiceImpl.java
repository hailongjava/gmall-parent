package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import javafx.stage.Screen;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        if(score%10==0){
            System.out.println("更新索引库：" + score);
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(score));//四舍五入
            goodsDao.save(goods);
        }
    }
}
