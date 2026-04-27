package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.ObjectResponse;

public class HttpHeaderParameterService {
    public void service(@HttpHeaderParameter("header") String header, ObjectResponse<String> response) {
        response.send(header);
    }
}
