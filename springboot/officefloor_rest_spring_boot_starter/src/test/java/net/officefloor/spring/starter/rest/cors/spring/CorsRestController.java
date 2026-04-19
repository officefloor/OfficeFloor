package net.officefloor.spring.starter.rest.cors.spring;

import org.springframework.web.bind.annotation.CrossOrigin;
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

}
