package net.officefloor.spring.starter.rest.web;

import net.officefloor.spring.starter.rest.web.spring.WebRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class NativeSpringWebTest extends AbstractWebVerification {

    @Override
    protected ServiceImplementation getServiceImplementation() {
        return ServiceImplementation.SPRING;
    }

    private @Autowired WebRestController restController;

    @Test
    public void direct()  throws Exception {
        ResponseEntity<String> response = this.restController.responseEntity();
        assertEquals(299, response.getStatusCode().value());
        assertEquals("Response Entity", response.getBody());
    }

}
