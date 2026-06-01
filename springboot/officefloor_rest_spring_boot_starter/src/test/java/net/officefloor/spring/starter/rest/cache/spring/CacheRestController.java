/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
