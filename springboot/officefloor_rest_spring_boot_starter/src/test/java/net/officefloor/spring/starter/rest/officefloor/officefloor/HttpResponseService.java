package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;

public class HttpResponseService {
    public void service(@HttpResponse(status = 201) ObjectResponse<String> response) {
        response.send("created");
    }
}
