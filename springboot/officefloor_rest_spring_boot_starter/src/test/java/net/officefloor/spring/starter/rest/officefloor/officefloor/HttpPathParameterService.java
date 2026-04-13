package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

public class HttpPathParameterService {
    public void service(@HttpPathParameter("id") String id, ObjectResponse<String> response) {
        response.send("ID=" + id);
    }
}
