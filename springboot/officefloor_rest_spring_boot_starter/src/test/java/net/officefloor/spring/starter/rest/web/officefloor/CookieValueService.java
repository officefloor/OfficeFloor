package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.CookieValue;

public class CookieValueService {
    public void service(@CookieValue(name = "biscuit") String cookie, ObjectResponse<String> response) {
        response.send(cookie);
    }
}
