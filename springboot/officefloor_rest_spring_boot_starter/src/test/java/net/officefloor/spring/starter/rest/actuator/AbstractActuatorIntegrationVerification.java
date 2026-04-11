package net.officefloor.spring.starter.rest.actuator;

import net.officefloor.spring.starter.rest.AbstractVerification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractActuatorIntegrationVerification extends AbstractVerification {

    private @Autowired TestRestTemplate client;

    @Test
    public void actuator() {
        ResponseEntity<String> response = this.client.getForEntity(this.getPath("/actuator/health", true), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"status\":\"UP\"}", response.getBody());
    }

}
