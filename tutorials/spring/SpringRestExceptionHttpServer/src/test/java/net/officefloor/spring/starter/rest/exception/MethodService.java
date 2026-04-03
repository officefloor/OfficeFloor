package net.officefloor.spring.starter.rest.exception;

// START SNIPPET: tutorial
public class MethodService {
    public void service() throws MockException {
        throw new MockException("thrown");
    }
}
// END SNIPPET: tutorial
