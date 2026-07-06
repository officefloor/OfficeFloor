package net.officefloor.tutorial.springrestexceptionhttpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// START SNIPPET: tutorial
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExceptionHandlingIntegrationTest {

    @Autowired
    private TestRestTemplate client;

    @Test
    public void method_exception_handling() {
        ResponseEntity<String> response = client.getForEntity("/exception/method", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Method handled: thrown", response.getBody());
    }

    @Test
    public void composition_exception_handling() {
        ResponseEntity<String> response = client.getForEntity("/exception/composition", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Composition handled: thrown", response.getBody());
    }

    @Test
    public void spring_controller_advice() {
        ResponseEntity<String> response = client.getForEntity("/exception/spring", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Spring handled: thrown", response.getBody());
    }

    @Test
    public void global_escalation_bad_request() {
        ResponseEntity<ProblemDetail> response = client.getForEntity("/exception/escalation", ProblemDetail.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals("thrown", body.getDetail());
    }

    @Test
    public void global_escalation_not_found() {
        ResponseEntity<ProblemDetail> response = client.getForEntity("/exception/not-found", ProblemDetail.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals("entity not found", body.getDetail());
    }
}
// END SNIPPET: tutorial
