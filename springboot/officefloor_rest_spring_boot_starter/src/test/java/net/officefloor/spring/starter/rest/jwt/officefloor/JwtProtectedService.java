package net.officefloor.spring.starter.rest.jwt.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.core.Authentication;

public class JwtProtectedService {
    public void service(Authentication authentication, ObjectResponse<String> response) {
        response.send(authentication.getName());
    }
}
