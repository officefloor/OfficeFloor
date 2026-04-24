package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.CookieValue;

public class CookieDefaultService {
    public void service(
            @CookieValue(name = "token", required = false, defaultValue = "default-cookie") String token,
            ObjectResponse<String> response) {
        response.send(token);
    }
}
