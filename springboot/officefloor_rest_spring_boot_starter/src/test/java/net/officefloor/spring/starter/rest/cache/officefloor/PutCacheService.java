package net.officefloor.spring.starter.rest.cache.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class PutCacheService {
    public void service(@RequestParam String key,
                        @RequestParam String value,
                        ObjectResponse<String> response) {
        // TODO: implement OfficeFloor cache put (store value under key)
        response.send(value);
    }
}
