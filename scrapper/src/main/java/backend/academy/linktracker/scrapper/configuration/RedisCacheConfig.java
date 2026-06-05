package backend.academy.linktracker.scrapper.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, Environment env) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        long defaultTtlMillis = parseLongOrDefault(env.getProperty("spring.cache.redis.time-to-live"), 600_000L);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTtlMillis))
                .serializeValuesWith(
                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        Map<String, RedisCacheConfiguration> initialConfigs = new HashMap<>();

        String userLinksTtl = env.getProperty("app.cache.userLinks.time-to-live");
        if (userLinksTtl != null) {
            long ms = parseLongOrDefault(userLinksTtl, defaultTtlMillis);
            initialConfigs.put("userLinks", defaultConfig.entryTtl(Duration.ofMillis(ms)));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(initialConfigs)
                .transactionAware()
                .build();
    }

    private long parseLongOrDefault(String value, long fallback) {
        if (value == null) return fallback;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            try {
                if (value.endsWith("ms")) {
                    return Long.parseLong(value.substring(0, value.length() - 2));
                }
                if (value.endsWith("s")) {
                    long seconds = Long.parseLong(value.substring(0, value.length() - 1));
                    return seconds * 1000L;
                }
            } catch (Exception ignored) {
            }
            return fallback;
        }
    }
}
