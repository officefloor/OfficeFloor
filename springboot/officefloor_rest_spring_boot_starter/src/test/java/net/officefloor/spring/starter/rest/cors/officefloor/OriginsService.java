package net.officefloor.spring.starter.rest.cors.officefloor;

import net.officefloor.web.ObjectResponse;

public class OriginsService {
    public void service(ObjectResponse<String> response) {
        response.send("origin");
    }
}
