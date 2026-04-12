package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.spring.starter.rest.web.common.MockException;

public class ControllerAdviceService {
    public void service() throws MockException {
        throw new MockException("TEST");
    }
}
