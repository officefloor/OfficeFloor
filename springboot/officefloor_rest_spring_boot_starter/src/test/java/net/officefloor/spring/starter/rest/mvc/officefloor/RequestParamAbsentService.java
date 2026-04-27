package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class RequestParamAbsentService {
    public void service(
            @RequestParam(name = "value", required = false) String value,
            ObjectResponse<String> response) {
        response.send(value != null ? value : "absent");
    }
}
