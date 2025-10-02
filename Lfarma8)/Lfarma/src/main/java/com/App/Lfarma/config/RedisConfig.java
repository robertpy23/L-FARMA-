package com.App.Lfarma.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.App.Lfarma.entity.Usuario;

// RedisConfig.java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Usuario> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Usuario> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Usuario.class));
        return template;
    }
}