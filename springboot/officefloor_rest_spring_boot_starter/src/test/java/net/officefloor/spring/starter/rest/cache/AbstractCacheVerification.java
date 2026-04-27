package net.officefloor.spring.starter.rest.cache;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractCacheVerification extends AbstractMockMvcVerification {

    // ── Item 1: @Cacheable computes on cache miss ─────────────────────────────

    @Test
    public void cacheableComputes() throws Exception {
        this.mvc.perform(get(getPath("/compute"))
                        .param("key", "cacheableComputes")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("cacheableComputes-computed")));
    }

    // ── Item 2: @CachePut stores a value; @Cacheable returns it on next call ──

    @Test
    public void cachePutAndGet() throws Exception {
        // Force a specific value into the cache
        this.mvc.perform(put(getPath("/put")).with(csrf())
                        .param("key", "cachePutAndGet")
                        .param("value", "cached-value"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("cached-value")));

        // @Cacheable should return the cached value, not recompute
        this.mvc.perform(get(getPath("/compute"))
                        .param("key", "cachePutAndGet")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("cached-value")));
    }

    // ── Item 3: @CacheEvict removes entry so next call recomputes ─────────────

    protected abstract int getEvictCacheStatus();

    @Test
    public void cacheEvictClearsCache() throws Exception {
        // Seed the cache with a value that differs from the computed one
        this.mvc.perform(put(getPath("/put")).with(csrf())
                        .param("key", "cacheEvictClearsCache")
                        .param("value", "initial"))
                .andExpect(status().isOk());

        // Evict that entry
        this.mvc.perform(delete(getPath("/evict")).with(csrf())
                        .param("key", "cacheEvictClearsCache"))
                .andExpect(status().is(this.getEvictCacheStatus()));

        // Must recompute now that the cache entry is gone
        this.mvc.perform(get(getPath("/compute"))
                        .param("key", "cacheEvictClearsCache")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("cacheEvictClearsCache-computed")));
    }
}
