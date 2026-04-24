package net.officefloor.spring.starter.rest.jwt.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtSecurityConfig {

    /**
     * Stateless JWT resource-server chain, scoped to /jwt/** paths and ordered
     * before the session-based form-login chain so JWT authentication takes
     * priority for those paths.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/spring/jwt/**", "/officefloor/jwt/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Test-only JwtDecoder backed by a static HMAC key. Real JWT validation is
     * bypassed in tests via MockMvc's jwt() post-processor; this bean exists
     * only so the application context starts without requiring an issuer URI.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(
            "test-jwt-secret-for-unit-tests-only-needs-256bits!!".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
