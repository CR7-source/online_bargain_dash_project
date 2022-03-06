package com.miaoshaproject.service;

/**
 * @Author:CR7-source
 * @Date: 2022/02/26/ 15:53
 * @Description
 */

public interface CacheService {
    //存方法
    void setCommonCache(String key,Object value);
    //存方法
    Object getCommonCache(String key);
}
