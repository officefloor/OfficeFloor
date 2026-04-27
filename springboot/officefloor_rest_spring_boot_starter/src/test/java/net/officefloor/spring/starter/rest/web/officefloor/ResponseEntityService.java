package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class ResponseEntityService {
    public void service(ObjectResponse<ResponseEntity<String>> response) {
        response.send(ResponseEntity.status(299).body("Response Entity"));
    }
}
