package net.officefloor.tutorial.springrestexceptionhttpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void global_escalation_handling() {
        ResponseEntity<String> response = client.getForEntity("/exception/escalation", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Escalation handled: thrown", response.getBody());
    }
}
// END SNIPPET: tutorial
