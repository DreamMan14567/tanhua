package com.tanhua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;


/**
 * @author user_Chubby
 * @date 2021/5/3
 * @Description
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class TanhuaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TanhuaServerApplication.class,args);
    }

    /**
     * redisTemplate默认的key序列化是jdk
     * 修改它为字符串
     * @param redisTemplate
     */
    @Resource
    public void setKeySerializer(RedisTemplate redisTemplate){
        redisTemplate.setKeySerializer(new StringRedisSerializer());
    }
}
