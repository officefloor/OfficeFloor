package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RedirectAttributesService {
    public void service(ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/mvc-destination?status=ok")
                .build());
    }
}
