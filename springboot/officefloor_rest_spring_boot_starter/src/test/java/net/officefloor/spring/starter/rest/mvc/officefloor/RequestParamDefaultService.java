package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class RequestParamDefaultService {
    public void service(
            @RequestParam(name = "value", required = false, defaultValue = "default-value") String value,
            ObjectResponse<String> response) {
        response.send(value);
    }
}
