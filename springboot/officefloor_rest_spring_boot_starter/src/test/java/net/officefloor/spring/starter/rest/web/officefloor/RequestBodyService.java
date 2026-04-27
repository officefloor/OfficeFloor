package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.spring.starter.rest.web.common.RequestBodyEntity;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

public class RequestBodyService {
    public void service(@RequestBody RequestBodyEntity requestBody, ObjectResponse<String> response) {
        response.send(requestBody.getRequest());
    }
}
