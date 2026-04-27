package net.officefloor.spring.starter.rest.validation.officefloor;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.MultiValidRequest;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

public class MultipleErrorsService {
    public void service(@Valid @RequestBody MultiValidRequest request,
                        BindingResult result,
                        ObjectResponse<ResponseEntity<String>> response) {
        if (result.hasErrors()) {
            response.send(ResponseEntity.badRequest().body("Errors: " + result.getErrorCount()));
        } else {
            response.send(ResponseEntity.ok("OK"));
        }
    }
}
