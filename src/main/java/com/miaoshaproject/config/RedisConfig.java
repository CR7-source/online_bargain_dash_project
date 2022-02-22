package com.miaoshaproject.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;
/**
 * @Author:CR7-source
 * @Date: 2022/02/14/ 16:58
 * @Description
 */
@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds=3600)
public class RedisConfig {

}
