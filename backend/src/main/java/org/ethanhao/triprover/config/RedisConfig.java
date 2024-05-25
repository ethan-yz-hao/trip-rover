package org.ethanhao.triprover.config;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.utils.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory
                                                               connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Note that the FastJsonRedisSerializer uses the LoginUser class
        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(LoginUser.class);
        // Use StringRedisSerializer to serialize and deserialize the key value of redis
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        // Hash key also uses StringRedisSerializer serialization method
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}