package net.officefloor.spring.starter.rest.security.officefloor;

import jakarta.annotation.security.RolesAllowed;
import net.officefloor.web.ObjectResponse;

public class RolesAllowedService {
    @RolesAllowed("ACCESS")
    public void service(ObjectResponse<String> response) {
        response.send("Accessed");
    }
}
