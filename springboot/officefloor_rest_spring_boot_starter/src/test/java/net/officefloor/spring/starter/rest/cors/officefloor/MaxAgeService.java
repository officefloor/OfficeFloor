package net.officefloor.spring.starter.rest.cors.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "https://example.com", maxAge = 3600)
public class MaxAgeService {
    public void service(ObjectResponse<String> response) {
        response.send("CORS");
    }
}
