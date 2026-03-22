package net.officefloor.spring.starter.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@RestController
public class MockRestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/hello/{user}")
    public ResponseEntity<String> hello(@PathVariable(name = "user") String user) {
        return ResponseEntity.ok("Hello " + user);
    }

    @GetMapping("/me")
    public String getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @GetMapping("/component")
    public String getComponent(MockComponent component) { return component.getValue(); }

    @PostMapping("/requestPart")
    public String getRequestPart(@RequestPart(name = "file") MultipartFile file) throws IOException {
        String content = IOUtil.readLines(file.getInputStream()).stream().collect(Collectors.joining());
        return "file=" + file.getOriginalFilename() + ", content=" + content;
    }

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidRequest {
        private @Min(1) int amount;
    }

    @GetMapping("/value")
    public String value(@Value("${officefloor.spring.test.value}") String propertyValue) {
        return propertyValue;
    }

    @GetMapping("/controllerAdvice")
    public String controllerAdvice() throws MockRestControllerAdvice.MockException {
        throw new MockRestControllerAdvice.MockException("TEST");
    }

    @GetMapping("/initBinder")
    public String initBinder(@RequestParam(name = "status") MockRestControllerAdvice.BindingTypes types) {
        switch (types) {
            case START:
                return "begin";
            case COMPLETE:
                return "end";
        }
        return null;
    }

}
