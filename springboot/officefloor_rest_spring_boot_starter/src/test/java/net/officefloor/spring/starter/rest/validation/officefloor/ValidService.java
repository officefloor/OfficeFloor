package net.officefloor.spring.starter.rest.validation.officefloor;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.springframework.web.bind.annotation.RequestBody;

import static org.junit.jupiter.api.Assertions.fail;

public class ValidService {
    public void service(@Valid @RequestBody ValidRequest request) {
        fail("Should not be invoked");
    }
}
