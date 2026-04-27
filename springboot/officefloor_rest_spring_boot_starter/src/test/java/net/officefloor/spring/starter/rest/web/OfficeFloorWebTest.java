package net.officefloor.spring.starter.rest.web;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorWebTest extends AbstractWebVerification {

    public void direct() {
        fail("TODO implement testing method directly");
    }
}
