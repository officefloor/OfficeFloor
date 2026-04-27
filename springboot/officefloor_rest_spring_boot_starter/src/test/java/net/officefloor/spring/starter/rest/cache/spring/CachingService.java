package net.officefloor.spring.starter.rest.cache.spring;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachingService {

    @Cacheable(value = "items", key = "#p0")
    public String compute(String key) {
        return key + "-computed";
    }

    @CachePut(value = "items", key = "#p0")
    public String put(String key, String value) {
        return value;
    }

    @CacheEvict(value = "items", key = "#p0")
    public void evict(String key) {
    }
}
