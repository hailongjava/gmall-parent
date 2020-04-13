package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

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
                new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    //根据二级分类的ID 获取三级分类的集合
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(
                new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    //根据一二三级分类的ID 查询平台属性（属性值集合）
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        // select * from base_attr_info inner join base_attr_value on ... where () or () or ()
        //Mybatis Plus  支持单表操作
        //解决办法 ： Mapper接口  Mapper.xml
        return baseAttrInfoMapper.attrInfoList(category1Id, category2Id, category3Id);
    }

    //保存平台属性及属性值
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1:保存平台属性 1
        baseAttrInfoMapper.insert(baseAttrInfo);
        //2:保存平台属性值表 多
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(attrValue -> {
                //平台属性表的ID 作为外键
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(attrValue);
            });
        }

    }

    //查询品牌集合 分页查询
    @Override
    public IPage<BaseTrademark> baseTrademark(Integer page, Integer limit) {
        //Mybaits-Plus

        //1:分页对象
        IPage<BaseTrademark> p = new Page(page, limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkMapper.selectPage(p, null);
        return baseTrademarkIPage;
    }


    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    //根据三级分类的ID  查询商品分页集合
    @Override
    public IPage<SpuInfo> spuPage(Integer page, Integer limit, Long category3Id) {
        //分页对象
        IPage<SpuInfo> p = new Page(page, limit);
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.
                selectPage(p, new QueryWrapper<SpuInfo>().eq("category3_id", category3Id));
        return spuInfoIPage;
    }

    //查询所有品牌的集合
    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }

    //查询所有销售属性
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }


    //保存商品信息
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1:保存Spu_info 商品信息表
        spuInfoMapper.insert(spuInfo);
        //2:保存商品图片表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(image -> {
            //商品信息表的ID  作为外键
            image.setSpuId(spuInfo.getId());
            spuImageMapper.insert(image);
        });
        //3:保存商品的销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(saleAttr -> {
            //商品信息表的ID  作为外键
            saleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(saleAttr);
            //4:此商品销售属性对应的N多个属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(saleAttrValue -> {
                //商品信息表的ID  作为外键
                saleAttrValue.setSpuId(spuInfo.getId());
                //销售属性的名称
                saleAttrValue.setSaleAttrName(saleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(saleAttrValue);
            });
        });
    }

    //根据spuId 查询图片列表
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
    }


    //根据spuId  查询销售属性及嵌套的属性值集合
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        //现在需要关联查询
        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }

    //保存Sku
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //默认是不卖
        skuInfo.setIsSale(0);
        //1:sku_info
        skuInfoMapper.insert(skuInfo);
        //2:sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(image -> {
            //外键
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insert(image);
        });
        //3:销售属性 sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
            //外键  sku_id
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            //外键  spu_id
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        });
        //4:平台属性 sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        skuAttrValueList.forEach(skuAttrValue -> {
            //外键
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        });
    }

    //查询Sku分页列表
    @Override
    public IPage<SkuInfo> skuList(Integer page, Integer limit) {
        return skuInfoMapper.selectPage(new Page<SkuInfo>(page, limit), null);
    }

    //上架
    @Override
    public void onSale(Long skuId) {
        //1:更新 库存 上架状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
        //2:保存索引
        //TODO

    }

    //下架
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
        //2:删除索引
        //TODO
    }


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    //根据skuId 查询库存表  使用Redisson进行上锁  防止缓存三大问题 都要解决
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //库存表保存在缓存中的Key的组成结构
        String key = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        //1:从缓存中获取SkuInfo信息
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
        if (null != skuInfo) {
            //2:有
            return skuInfo;
        }
        //上锁    缓存三大问题之一缓存击穿 百万请求只允许派一个代表来查询Mysql数据 其它不让进
        RLock lock = redissonClient.getLock(key + ":lock");
        try {
            boolean tryLock = lock.tryLock(1, 10, TimeUnit.SECONDS);//  在10秒内未获取锁 证明：在我之前此锁已经被别人获取到了  别人就代表
            if (tryLock) {
                //我是代表：我是第一人 拿到锁了
                skuInfo = skuInfoMapper.selectById(skuId);
                //缓存三大问题之一的穿透
                if (null == skuInfo) {
                    //空结果
                    skuInfo = new SkuInfo();
                    redisTemplate.opsForValue().set(key, skuInfo, 5, TimeUnit.MINUTES);
                    return skuInfo;
                } else {
                    //查询Sku图片
                    List<SkuImage> skuImageList = skuImageMapper.
                            selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                    skuInfo.setSkuImageList(skuImageList);
                    //缓存三大问题之雪崩
//                    Random r = new Random();
//                    r.
                    redisTemplate.opsForValue().set(key, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return skuInfo;
                }
            } else {
                //我不是第一人 也不是代表
                Thread.sleep(2000);
                return (SkuInfo) redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //解锁
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        //在抛出异常时  会执行此处代码
        return getSkuInfoDBById(skuId);
    }

    //提取根据库存Id 查询Mysql数据库的SkuInfo
    public SkuInfo getSkuInfoDBById(Long skuId) {
        //此处也会出现穿透的情况：
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(null == skuInfo){
            skuInfo = new SkuInfo();
            return skuInfo;
        }
        List<SkuImage> skuImageList = skuImageMapper.
                selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    //根据skuId 查询库存表 普通的实现
//    public SkuInfo getSkuInfo(Long skuId) {
//        //1:先去Redis缓存中获取   五大数据类型 ： 常用 String类型  Hash类型  偶尔使用List
//        String key = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
//        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
//        if(null != skuInfo){
//            System.out.println("缓存中已经有数据了：" + JSON.toJSONString(skuInfo));
//            //2:有 直接返回
//            return skuInfo;
//        }
//
//        System.out.println("缓存中没有数据");
//        //3:没有 再去Mysql数据库中查询
//        //1)根据SKUID查询库存表
//        SkuInfo skuInfo1 = skuInfoMapper.selectById(skuId);
//        //判断获取Mysql数据中SkuInfo是否为NULL  为NULL表示人为攻击 返回空结果
//        if(null == skuInfo1){
//            skuInfo1 = new SkuInfo();
//            System.out.println("有人攻击我们网站：skuId不存在：返回空结果");
//            redisTemplate.opsForValue().set(key,skuInfo1,5, TimeUnit.MINUTES);//过期时间为5分钟
//            return skuInfo;
//        }
//        //2)根据SKUID查询库存图片表
//        List<SkuImage> skuImageList = skuImageMapper.
//                selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
//        skuInfo1.setSkuImageList(skuImageList);
//        System.out.println("缓存中再次保存了一份数据：" + JSON.toJSONString(skuInfo));
//
//        //随机数
//        Random random = new Random();
//        int i = random.nextInt(300);
//        //4:再保存缓存一份
//        redisTemplate.opsForValue().set(key,skuInfo1,
//                RedisConst.SKUKEY_TIMEOUT + i,TimeUnit.SECONDS);//key:String类型 V：任何类型 底层转成JSon格式字符串
//        //5:返回
//        return skuInfo1;
//    }


    //根据三级分类的ID 查询一二三级分类的ID、名称
    @Override
    @GmallCache(prefix="getBaseCategoryView")
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    //单独查询价格
    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (null != skuInfo) {
            //防止NULL指针异常
            return skuInfo.getPrice();
        }
        return null;
    }

    //-- 根据商品ID查询销售属性及销售属性值集合
    //-- 并且根据当前skuId库存ID查询出对应的销售属性值
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {

        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //查询组合对应库存ID
    // {颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId,颜色|版本|套装 : skuId}
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map result = new HashMap();
        List<Map> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        //Map1  K:values_id V:1|13|11   K：sku_id V:10
        skuValueIdsMap.forEach(map -> {
            result.put(map.get("values_id"), map.get("sku_id"));
        });
        return result;
    }


    //查询分类视图对象集合 （查询全部）
    @Override
    public List<Map> getBaseCategoryList() {
        //结果对象
        List<Map> result = new ArrayList<>();

        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //一级分类ID进行分组查询
        Map<Long, List<BaseCategoryView>> baseCategoryViewByCategory1Id = baseCategoryViewList.
                stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //Map1 k: 1 V:List<BaseCategoryView>  60  ID:1-60
        //Map2 k: 2 V:List<BaseCategoryView>  24 ID:61-85
        //定义角标
        int index = 1;
        for (Map.Entry<Long, List<BaseCategoryView>> category1IdEntry :
                baseCategoryViewByCategory1Id.entrySet()) {
            Map  category1IdMap = new HashMap();
            //1:角标
            category1IdMap.put("index",index++);
            //2:一级分类的Id
            category1IdMap.put("categoryId",category1IdEntry.getKey());
            //3:一级分类的名称
            category1IdMap.put("categoryName",category1IdEntry.getValue().get(0).getCategory1Name());
            //4:二级分类的子节点
            //4-1:查询二级分类的集合
            Map<Long, List<BaseCategoryView>> baseCategoryViewByCategory2Id = category1IdEntry.getValue().stream().collect(
                    Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //Map1 K:二级分类的ID V：  长度是4
            List<Map> result2 = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2IdEntry : baseCategoryViewByCategory2Id.entrySet()) {
                Map category2IdMap = new HashMap();
                //三级分类的集合
                List<BaseCategoryView> category3IdValue = category2IdEntry.getValue();

                //1:二级分类的ID
                category2IdMap.put("categoryId",category2IdEntry.getKey());
                //2:二级分类的名称
                category2IdMap.put("categoryName", category3IdValue.get(0).getCategory2Name());
                //3:三级分类的集合
                //4-1-3
                List<Map> result3 = new ArrayList<>();
                for (BaseCategoryView baseCategoryView : category3IdValue) {
                    Map category3IdMap = new HashMap();
                    //1:三级分类的ID
                    category3IdMap.put("categoryId",baseCategoryView.getCategory3Id());
                    //2:三级分类的名称
                    category3IdMap.put("categoryName",baseCategoryView.getCategory3Name());
                    result3.add(category3IdMap);
                }
                category2IdMap.put("categoryChild",result3);
                result2.add(category2IdMap);
            }
            category1IdMap.put("categoryChild",result2);
            result.add(category1IdMap);
        }
        return result;
    }

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
}
