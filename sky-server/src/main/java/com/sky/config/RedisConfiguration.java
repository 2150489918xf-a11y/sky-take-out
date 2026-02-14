package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 创建RedisTemplate对象,并将其注入到Spring容器中,以便我们在其他地方使用RedisTemplate来操作Redis数据库
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("正在创建RedisTemplate对象");
        // 创建RedisTemplate对象
        RedisTemplate redisTemplate = new RedisTemplate();
        // 设置连接工厂,因为RedisTemplate需要通过连接工厂来获取连接对象,才能操作Redis数据库,所以我们需要将连接工厂注入到RedisTemplate中
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置序列化器,因为默认的序列化器是JdkSerializationRedisSerializer,它会将对象序列化成二进制数据,不方便查看和调试,所以我们使用StringRedisSerializer来序列化字符串数据
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        log.info("RedisTemplate对象创建成功");
        return redisTemplate;

    }
}
