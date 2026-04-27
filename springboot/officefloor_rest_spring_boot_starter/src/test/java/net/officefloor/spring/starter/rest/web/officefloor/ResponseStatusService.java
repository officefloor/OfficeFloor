package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResponseStatusService {
    @ResponseStatus(HttpStatus.CREATED)
    public void service(ObjectResponse<String> response) {
        response.send("Response Status");
    }
}
