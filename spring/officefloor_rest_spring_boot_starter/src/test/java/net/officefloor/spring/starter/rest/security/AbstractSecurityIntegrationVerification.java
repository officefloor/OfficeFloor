package net.officefloor.spring.starter.rest.security;

import net.officefloor.spring.starter.rest.AbstractVerification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractSecurityIntegrationVerification extends AbstractVerification {

    private @Autowired TestRestTemplate client;

    @Test
    public void hello() {
        ResponseEntity<String> response = this.client.getForEntity(this.getPath("/hello/User_1"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello User_1", response.getBody());
    }

    @Test
    public void userDetails() {
        ResponseEntity<String> response = this.client.exchange(this.getPath("/userDetails"), HttpMethod.GET, new HttpEntity<>(this.getAuthenticatedHttpHeaders("user", "password")), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user", response.getBody());
    }

    private HttpHeaders getAuthenticatedHttpHeaders(String username, String password) {

        // Initiate login
        ResponseEntity<String> loginResponse = this.client.getForEntity("/login", String.class);

        // Obtain the session cookie
        String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        // Get CSRF token
        String loginPageHtml = loginResponse.getBody();
        final String CSRF_CONTENT = "name=\"_csrf\" value=\"";
        int start = loginPageHtml.indexOf(CSRF_CONTENT) + CSRF_CONTENT.length();
        int end = loginPageHtml.indexOf("\"", start);
        String csrfToken = loginPageHtml.substring(start, end);

        // Authenticate to obtain cookie
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "user");
        form.add("password", "password");
        form.add("_csrf", csrfToken);
        HttpHeaders authenticateHeaders = new HttpHeaders();
        authenticateHeaders.add(HttpHeaders.COOKIE, sessionCookie);
        authenticateHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Void> authenticateResponse = this.client
                .withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW)
                .postForEntity(this.getPath("/login", true), new HttpEntity<>(form, authenticateHeaders), Void.class);
        assertEquals(302, authenticateResponse.getStatusCode().value(), "Should be successful authenticate");

        // New session after login
        sessionCookie = authenticateResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        // Create the authenticated headers
        HttpHeaders authenticatedHeaders = new HttpHeaders();
        authenticatedHeaders.add(HttpHeaders.COOKIE, sessionCookie);
        return authenticatedHeaders;
    }

}
