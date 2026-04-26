package net.officefloor.spring.starter.rest.cache;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class NativeSpringCacheTest extends AbstractCacheVerification {

    @Override
    protected int getEvictCacheStatus() {
        return 200; // DELETE with no response should be sending 204
    }
}
