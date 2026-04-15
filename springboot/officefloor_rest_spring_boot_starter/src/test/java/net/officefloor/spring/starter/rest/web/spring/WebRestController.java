package net.officefloor.spring.starter.rest.web.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.spring.starter.rest.web.common.BindingTypes;
import net.officefloor.spring.starter.rest.web.common.MockComponent;
import net.officefloor.spring.starter.rest.web.common.MockException;
import net.officefloor.spring.starter.rest.web.common.RequestBodyEntity;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/spring/web")
public class WebRestController {

    @GetMapping("/path/{id}")
    public String pathVariable(@PathVariable("id") Integer id) {
        return "ID=" + id;
    }

    @GetMapping("/query")
    public String requestParam(@RequestParam("name") String name) {
        return name;
    }

    @GetMapping("/header")
    public String requestHeader(@RequestHeader("header") String header) {
        return header;
    }

    @GetMapping("/cookie")
    public String cookieValue(@CookieValue("biscuit") String buscuit) {
        return buscuit;
    }

    @PostMapping("/requestBody")
    public String requestBody(@RequestBody RequestBodyEntity requestBody) {
        return requestBody.getRequest();
    }

    @GetMapping("/responseStatus")
    @ResponseStatus(HttpStatus.CREATED)
    public String responseStatus() {
        return "Response Status";
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
    public String initBinder(@RequestParam(name = "status") BindingTypes types) {
        String response = switch (types) {
            case START -> "begin";
            case COMPLETE -> "end";
            default -> null;
        };
        return response;
    }

    @GetMapping("/httpServletRequest")
    public String httpServletRequest(HttpServletRequest request) {
        return request.getParameter("name");
    }

    @GetMapping("/httpServletResponse")
    public void httpServletResponse(HttpServletResponse response) throws IOException {
        response.getWriter().write("Servlet");
    }

    @GetMapping("/cors")
    @CrossOrigin(origins = "https://example.com")
    public String cors() {
        return "CORS";
    }

}
