package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestHeader;

public class RequestHeaderService {
    public void service(@RequestHeader(name = "header") String header, ObjectResponse<String> response) {
        response.send(header);
    }
}
