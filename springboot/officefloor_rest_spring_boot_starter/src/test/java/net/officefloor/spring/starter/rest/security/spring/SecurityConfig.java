package net.officefloor.spring.starter.rest.security.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                    // Only secure security tests (and any tests looking to integrate security)
                    .requestMatchers("/spring/security/hello/*", "/officefloor/security/hello/*").permitAll()
                    .requestMatchers("/spring/security/*", "/officefloor/security/*", "*/secure/*").authenticated()
                    // Avoid security leaking into other tests focused on different functionality
                    .anyRequest().permitAll()
            )
            .formLogin((form) -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/spring/security/greeting", true)
                    .permitAll()
            )
            .logout(LogoutConfigurer::permitAll)
            // CORS tested elsewhere
            .cors((cors) -> cors.disable());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        String password = encoder.encode("password");
        UserDetails user = User.withUsername("user").password(password).roles("USER").build();
        return new InMemoryUserDetailsManager(user);
    }
}
