package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;

public class SecureService {
    public void service(ViewResponse response) {
        response.send("secure");
    }
}
