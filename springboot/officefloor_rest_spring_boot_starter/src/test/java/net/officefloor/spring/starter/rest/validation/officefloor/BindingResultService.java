package net.officefloor.spring.starter.rest.validation.officefloor;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

public class BindingResultService {
    public void service(@Valid @RequestBody ValidRequest request, BindingResult result, ObjectResponse<ResponseEntity<String>> response) {
        if (result.hasErrors()) {
            response.send(ResponseEntity.badRequest().body("Errors: " + result.getErrorCount()));
        } else {
            response.send(ResponseEntity.ok("OK"));
        }
    }
}
