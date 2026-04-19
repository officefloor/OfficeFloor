package net.officefloor.spring.starter.rest.cors;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractCorsVerification extends AbstractMockMvcVerification {

    // ---- @CrossOrigin annotation happy-day tests ----

    @Test
    public void origins() throws Exception {
        this.mvc.perform(get(this.getPath("/origins"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void originPattern() throws Exception {
        this.mvc.perform(get(this.getPath("/origin-pattern"))
                        .header("Origin", "https://sub.example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://sub.example.com"))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void allowedMethods() throws Exception {
        this.mvc.perform(options(this.getPath("/allowed-methods"))
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
    }

    @Test
    public void allowedHeaders() throws Exception {
        this.mvc.perform(options(this.getPath("/allowed-headers"))
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Custom-Header"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("X-Custom-Header")));
    }

    @Test
    public void exposedHeaders() throws Exception {
        this.mvc.perform(get(this.getPath("/exposed-headers"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("X-Custom-Header")))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void allowCredentials() throws Exception {
        this.mvc.perform(get(this.getPath("/allow-credentials"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void maxAge() throws Exception {
        this.mvc.perform(options(this.getPath("/max-age"))
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    // ---- @CrossOrigin annotation negative tests ----

    @Test
    public void originRejected() throws Exception {
        this.mvc.perform(get(this.getPath("/origins"))
                        .header("Origin", "https://evil.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void preflightMethodRejected() throws Exception {
        this.mvc.perform(options(this.getPath("/allowed-methods"))
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void preflightHeaderRejected() throws Exception {
        this.mvc.perform(options(this.getPath("/allowed-headers"))
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Forbidden-Header"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    // ---- WebMvcConfigurer.addCorsMappings() tests ----

    @Test
    public void mvcConfigurerAllowed() throws Exception {
        this.mvc.perform(get(this.getPath("/mvc-configurer/origin"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void mvcConfigurerRejected() throws Exception {
        this.mvc.perform(get(this.getPath("/mvc-configurer/origin"))
                        .header("Origin", "https://evil.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    // ---- CorsConfigurationSource bean tests ----

    @Test
    public void corsConfigSourceAllowed() throws Exception {
        this.mvc.perform(get(this.getPath("/cors-config-source/origin"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(content().string(equalTo("CORS")));
    }

    @Test
    public void corsConfigSourceRejected() throws Exception {
        this.mvc.perform(get(this.getPath("/cors-config-source/origin"))
                        .header("Origin", "https://evil.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

}
