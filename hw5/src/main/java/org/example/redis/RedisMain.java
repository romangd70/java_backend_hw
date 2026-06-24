package org.example.redis;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.time.Duration;
import java.util.Iterator;

@ComponentScan(basePackages = "org.example.redis")
@Configuration
@EnableCaching
@EnableRedisRepositories
public class RedisMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.redis");
        RedisTemplate<String, Object> redisTemplate = context.getBean(RedisTemplate.class);
        StudentRepository studentRepository = context.getBean(StudentRepository.class);
        CacheService cacheService = context.getBean(CacheService.class);

//        redisTemplateTest(redisTemplate);
//        redisRepositoryTest(studentRepository);
        redisCacheTest(cacheService);
    }

    public static void redisCacheTest(CacheService cacheService) {
        System.out.println("FirstRun");
        System.out.println(cacheService.calculate("1"));
        System.out.println("SecondRun");
        System.out.println(cacheService.calculate("1"));

        cacheService.evict("1");
        System.out.println("ThirdRun");
        System.out.println(cacheService.calculate("1"));
    }

    public static void redisTemplateTest(RedisTemplate<String, Object> redisTemplate) {
        redisTemplate.opsForValue().set("1", "John");
        Iterator<String> keys = redisTemplate.keys("*").iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Object o = redisTemplate.opsForValue().get(key);
            System.out.println(o);
        }
    }

    public static void redisRepositoryTest(StudentRepository studentRepository) {
        Student student = new Student();
        student.setId("1");
        student.setName("John");
        student.setGender(Student.Gender.MALE);
        student.setGrade(10);
        studentRepository.save(student);
        System.out.println(studentRepository.findById("1"));
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.setHostName("localhost");
        jedisConFactory.setPort(6379);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // время жизни кэша
            .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
