package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;

public class FilterCheckService {
    public void service(ObjectResponse<String> response) {
        response.send("ok");
    }
}
