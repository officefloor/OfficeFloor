package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.access.prepost.PreAuthorize;

public class PreAuthorizeService {
    @PreAuthorize("hasRole('ACCESS')")
    public void service(ObjectResponse<String> response) {
        response.send("Accessed");
    }
}
