package net.officefloor.spring.starter.rest.cache.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class ComputeService {
    public void service(@RequestParam String key,
                        ObjectResponse<String> response) {
        response.send(key + "-computed");
    }
}
