package net.officefloor.spring.starter.rest.jwt.spring;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring/jwt")
public class JwtRestController {

    // Returns the JWT subject — proves the token was decoded and authenticated
    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    // Requires ROLE_ADMIN authority in the JWT claims
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "admin-accessed";
    }
}
