package com.miaoshaproject.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaoshaproject.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @Author:CR7-source
 * @Date: 2022/02/26/ 15:55
 * @Description
 */
@Service
public class CacheServiceImpl implements CacheService {
    private Cache<String,Object> commonCache=null;
    @PostConstruct
    public void init(){
        commonCache= CacheBuilder.newBuilder()
                //设置缓存的初始容量为10
                .initialCapacity(10)
                //设置缓存的最大容量为100个key，超过之后会按照LRU的 策略
                .maximumSize(100)
                .expireAfterWrite(30,TimeUnit.SECONDS).build()
        ;
    }
    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key,value);
    }

    @Override
    public Object getCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
