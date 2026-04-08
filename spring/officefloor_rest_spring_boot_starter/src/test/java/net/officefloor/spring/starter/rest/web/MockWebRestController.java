package net.officefloor.spring.starter.rest.web;

import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/spring/web")
public class MockWebRestController {

    @GetMapping("/path/{id}")
    public String pathParameter(@PathVariable("id") Integer id) {
        return "ID=" + id;
    }

    @GetMapping("/query")
    public String queryParameter(@RequestParam("name") String name) {
        return name;
    }

    @GetMapping("/header")
    public String header(@RequestHeader("header") String header) {
        return header;
    }

    @GetMapping("/cookie")
    public String cookie(@CookieValue("biscuit") String buscuit) {
        return buscuit;
    }

    @PostMapping("/requestBody")
    public String requestBody(@RequestBody RequestBodyEntity requestBody) {
        return requestBody.getRequest();
    }

    @GetMapping("/responseEntity")
    public ResponseEntity<String> responseEntity() {
        return ResponseEntity.status(299).body("Response Entity");
    }

    @GetMapping("/component")
    public String component(MockComponent component) { return component.getValue(); }

    @PostMapping("/requestPart")
    public String requestPart(@RequestPart(name = "file") MultipartFile file) throws IOException {
        String content = IOUtil.readLines(file.getInputStream()).stream().collect(Collectors.joining());
        return "file=" + file.getOriginalFilename() + ", content=" + content;
    }

    @GetMapping("/propertyValue")
    public String value(@Value("${officefloor.spring.test.value}") String propertyValue) {
        return propertyValue;
    }

    @GetMapping("/controllerAdvice")
    public String controllerAdvice() throws MockException {
        throw new MockException("TEST");
    }

    @GetMapping("/initBinder")
    public String initBinder(@RequestParam(name = "status") MockBindingTypes types) {
        switch (types) {
            case START:
                return "begin";
            case COMPLETE:
                return "end";
        }
        return null;
    }

}
