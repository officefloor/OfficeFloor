package net.officefloor.spring.starter.rest.jwt.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.access.prepost.PreAuthorize;

public class JwtAdminService {
    @PreAuthorize("hasRole('ADMIN')")
    public void service(ObjectResponse<String> response) {
        response.send("admin-accessed");
    }
}
