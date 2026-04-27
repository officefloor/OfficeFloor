package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestHeader;

public class RequestHeaderDefaultService {
    public void service(
            @RequestHeader(name = "X-Optional", required = false, defaultValue = "default-header") String header,
            ObjectResponse<String> response) {
        response.send(header);
    }
}
