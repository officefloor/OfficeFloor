package net.officefloor.spring.starter.rest.cors.spring;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring/cors")
public class CorsRestController {

    @GetMapping("/origins")
    @CrossOrigin(origins = "https://example.com")
    public String origins() {
        return "CORS";
    }

    @GetMapping("/origin-pattern")
    @CrossOrigin(originPatterns = "https://*.example.com")
    public String originPattern() {
        return "CORS";
    }

    @DeleteMapping("/allowed-methods")
    @CrossOrigin(origins = "https://example.com")
    public String allowedMethods() {
        return "CORS";
    }

    @GetMapping("/allowed-headers")
    @CrossOrigin(origins = "https://example.com", allowedHeaders = "X-Custom-Header")
    public String allowedHeaders() {
        return "CORS";
    }

    @GetMapping("/exposed-headers")
    @CrossOrigin(origins = "https://example.com", exposedHeaders = "X-Custom-Header")
    public String exposedHeaders() {
        return "CORS";
    }

    @GetMapping("/allow-credentials")
    @CrossOrigin(origins = "https://example.com", allowCredentials = "true")
    public String allowCredentials() {
        return "CORS";
    }

    @GetMapping("/max-age")
    @CrossOrigin(origins = "https://example.com", maxAge = 3600)
    public String maxAge() {
        return "CORS";
    }

    @GetMapping("/mvc-configurer/origin")
    public String mvcConfigurerOrigin() {
        return "CORS";
    }

    @GetMapping("/cors-config-source/origin")
    public String corsConfigSourceOrigin() {
        return "CORS";
    }

}
