package org.example.redis;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    @SneakyThrows
    @Cacheable(value = "cache_test")
    public int calculate(String id) {
        Thread.sleep(10_000);
        return 10;
    }

    @CacheEvict(value = "cache_test")
    public void evict(String id) {
        System.out.println("Evict");
    }
}
