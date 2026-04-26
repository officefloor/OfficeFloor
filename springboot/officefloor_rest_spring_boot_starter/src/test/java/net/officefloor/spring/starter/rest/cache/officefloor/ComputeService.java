package net.officefloor.spring.starter.rest.cache.officefloor;

import net.officefloor.spring.starter.rest.cache.spring.CachingService;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class ComputeService {
    public void service(@RequestParam(name = "key") String key,
                        CachingService cachingService,
                        ObjectResponse<String> response) {
        response.send(cachingService.compute(key));
    }
}
