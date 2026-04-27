package net.officefloor.spring.starter.rest.async.officefloor;

import net.officefloor.web.ObjectResponse;

public class AsyncSpringServiceStub {
    public void service(ObjectResponse<String> response) {
        // TODO: implement OfficeFloor @Async service integration
        response.send("async-service-result");
    }
}
