package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.core.Authentication;

public class AuthenticationService {
    public void service(Authentication authentication, ObjectResponse<String> response) {
        response.send(authentication.getName());
    }
}
