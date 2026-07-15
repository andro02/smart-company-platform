package com.smartcompany.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfig {

    // Jackson ne zna da rekonstruiše PageImpl (Page je interfejs) bez ovoga —
    // ovaj mixin kaže tačno koji konstruktor da pozove pri deserijalizaciji
    // keširane vrednosti nazad iz Redisa.
    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class PageJacksonMixin {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public PageJacksonMixin(@JsonProperty("content") List<?> content,
                                 @JsonProperty("number") int number,
                                 @JsonProperty("size") int size,
                                 @JsonProperty("totalElements") long totalElements) {
        }
    }

    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Default typing je neophodan da Jackson upiše tačnu klasu (npr.
        // ProductDto, WarehouseDto) u JSON, jer @Cacheable vrednosti u
        // Redisu čuvamo generički (Object), pa bez ovoga ne bi znao u šta
        // da deserijalizuje nazad.
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.smartcompany.")
                .allowIfSubType("java.util.")
                .allowIfSubType("org.springframework.data.domain.")
                .build();
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        mapper.addMixIn(PageImpl.class, PageJacksonMixin.class);
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Kraći TTL za resurse čije se isOnline/lastHeartbeat stanje menja
        // često preko simulatora — sprečava da keš prikaže zastareo status
        // predugo. Duži TTL za podatke koji se retko menjaju (kategorije).
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("product", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("factories", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("warehouses", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("vehicles", defaultConfig.entryTtl(Duration.ofSeconds(60)));


        cacheConfigs.put("countries", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("cities", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("vehicle-brands", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("vehicle-models", defaultConfig.entryTtl(Duration.ofHours(24)));

        cacheConfigs.put("managers", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}