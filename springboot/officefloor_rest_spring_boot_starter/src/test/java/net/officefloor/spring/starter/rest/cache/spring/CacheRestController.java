package net.officefloor.spring.starter.rest.cache.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring/cache")
public class CacheRestController {

    private @Autowired CachingService cachingService;

    // @Cacheable — returns cached value if present, computes and caches on miss
    @GetMapping("/compute")
    public String compute(@RequestParam(name = "key") String key) {
        return cachingService.compute(key);
    }

    // @CachePut — always calls the service and stores the result in cache
    @PutMapping("/put")
    public String put(@RequestParam(name = "key") String key,
                      @RequestParam(name = "value") String value) {
        return cachingService.put(key, value);
    }

    // @CacheEvict — removes the entry so the next compute call recomputes
    @DeleteMapping("/evict")
    public void evict(@RequestParam(name = "key") String key) {
        cachingService.evict(key);
    }
}
