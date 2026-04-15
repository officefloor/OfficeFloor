package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.CrossOrigin;

public class CorsService {
    @CrossOrigin(origins = "https://example.com")
    public void service(ObjectResponse<String> response) {
        response.send("CORS");
    }
}
