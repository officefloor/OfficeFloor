package net.officefloor.spring.starter.rest.async.officefloor;

import net.officefloor.web.ObjectResponse;

public class CallableService {
    public void service(ObjectResponse<String> response) {
        response.send("callable-result");
    }
}
