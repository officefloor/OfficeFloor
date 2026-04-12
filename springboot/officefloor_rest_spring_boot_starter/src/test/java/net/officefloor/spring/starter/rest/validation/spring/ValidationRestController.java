package net.officefloor.spring.starter.rest.validation.spring;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.fail;

@RestController
@RequestMapping("/spring/validation")
public class ValidationRestController {

    @PostMapping("/valid")
    public String valid(@Valid @RequestBody ValidRequest request) {
        return fail("Should not be invoked");
    }

    @PostMapping("/bindingResult")
    public ResponseEntity<String> bindingResult(@Valid @RequestBody ValidRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errors: " + result.getErrorCount());
        }
        return ResponseEntity.ok("OK");
    }

}
