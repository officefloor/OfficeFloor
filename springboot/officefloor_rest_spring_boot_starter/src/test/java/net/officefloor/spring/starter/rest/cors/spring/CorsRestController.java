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
        return "origin";
    }

    @GetMapping("/origin-pattern")
    @CrossOrigin(originPatterns = "https://*.example.com")
    public String originPattern() {
        return "originPattern";
    }

    @DeleteMapping("/allowed-methods")
    @CrossOrigin(origins = "https://example.com")
    public String allowedMethods() {
        return "allowedMethods";
    }

    @GetMapping("/allowed-headers")
    @CrossOrigin(origins = "https://example.com", allowedHeaders = "X-Custom-Header")
    public String allowedHeaders() {
        return "allowedHeaders";
    }

    @GetMapping("/exposed-headers")
    @CrossOrigin(origins = "https://example.com", exposedHeaders = "X-Custom-Header")
    public String exposedHeaders() {
        return "exposedHeaders";
    }

    @GetMapping("/allow-credentials")
    @CrossOrigin(origins = "https://example.com", allowCredentials = "true")
    public String allowCredentials() {
        return "allowCredentials";
    }

    @GetMapping("/max-age")
    @CrossOrigin(origins = "https://example.com", maxAge = 3600)
    public String maxAge() {
        return "maxAge";
    }

    @GetMapping("/mvc-configurer/origin")
    public String mvcConfigurerOrigin() {
        return "mvcConfigurerOrigin";
    }

    @GetMapping("/cors-config-source/origin")
    public String corsConfigSourceOrigin() {
        return "corsConfigSourceOrigin";
    }

}
