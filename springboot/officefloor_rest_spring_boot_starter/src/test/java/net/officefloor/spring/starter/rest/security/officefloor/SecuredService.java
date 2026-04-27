package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.access.annotation.Secured;

public class SecuredService {
    @Secured("ROLE_ACCESS")
    public void service(ObjectResponse<String> response) {
        response.send("Accessed");
    }
}
