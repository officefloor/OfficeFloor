package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class RequestParamService {
    public void service(@RequestParam(name = "name") String name, ObjectResponse<String> response) {
        response.send(name);
    }
}
