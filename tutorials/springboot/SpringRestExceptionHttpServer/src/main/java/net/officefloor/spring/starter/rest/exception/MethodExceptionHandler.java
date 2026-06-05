package net.officefloor.spring.starter.rest.exception;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class MethodExceptionHandler {
    public void handle(@Parameter MockException ex, ObjectResponse<String> response) {
        response.send("Method handled: " + ex.getMessage());
    }
}
// END SNIPPET: tutorial
