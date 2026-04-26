package net.officefloor.spring.starter.rest.cache;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorCacheTest extends AbstractCacheVerification {

    @Override
    protected int getEvictCacheStatus() {
        return 204; // correct status given DELETE with no response body
    }
}
