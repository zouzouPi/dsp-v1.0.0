package com.cetiti.dsp.service.impl;

import com.cetiti.core.annotation.LogInject;
import com.cetiti.core.cache.RedisCache;
import com.cetiti.core.support.BaseSupport;
import com.cetiti.dsp.dao.GoodsDao;
import com.cetiti.dsp.entity.Goods;
import com.cetiti.dsp.service.GoodsService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl extends BaseSupport implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Autowired
    private RedisCache cache;
    @LogInject
    private Logger LOG;

    @Override
    public List<Goods> getGoodsList() {
        String cache_key = "getGoodsList";
        List<Goods> result_cache = cache.getListCache(cache_key,Goods.class);
        if(!isNull(result_cache)){
            LOG.info("get cache with key:" + cache_key);
        } else {
            result_cache = goodsDao.queryAll();
            cache.putListCache(cache_key,result_cache);
            LOG.info("put cache with key:" + cache_key);
            return result_cache;
        }
        return  result_cache;
    }

    @Override
    public List<Goods> getGoodsListByHm() {
        String cache_key = "getGoodsListByHm";
        List<Goods> result_cache = cache.getHMCacheAll4List(cache_key,Goods.class);
        if(!isNull(result_cache)){
            LOG.info("get cache with key:" + cache_key);
        } else {
            result_cache = goodsDao.queryAll();
            Map<String,Goods> map =new HashMap<>();
            for(Goods goods : result_cache){
                map.put(goods.getTitle(),goods);
            }
            cache.putHmCacheAll(cache_key,map);
            LOG.info("put cache with key:" + cache_key);
            return result_cache;
        }
        return  result_cache;
    }

    @Override
    public int reduceGoods(long goodsId) {
        int result = goodsDao.reduceGoods(goodsId);
        if(result <= 0){
            LOG.error("reduceGoods erro");
        }else{
            //删除
            cache.deleteHmCache("getGoodsListByHm","ipad3");
            LOG.info("delete cache with key: getGoodsListByHm|ipad3");
            //添加
            List<Goods> result_cache = goodsDao.queryAll();
            for(Goods goods : result_cache){
                if(goods.getTitle().equals("ipad3")){
                    cache.putHmCache("getGoodsListByHm","ipad3",goods);
                }
            }
        }
        return result;
    }

}
