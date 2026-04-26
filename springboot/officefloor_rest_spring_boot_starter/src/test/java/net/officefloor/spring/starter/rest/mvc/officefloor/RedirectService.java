package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RedirectService {
    public void service(ViewResponse response) {
        response.send("redirect:/mvc-destination");
    }
}
