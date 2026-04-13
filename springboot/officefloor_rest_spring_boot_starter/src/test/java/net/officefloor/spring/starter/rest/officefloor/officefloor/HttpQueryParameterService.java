package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

public class HttpQueryParameterService {
    public void service(@HttpQueryParameter("name") String name, ObjectResponse<String> response) {
        response.send(name);
    }
}
