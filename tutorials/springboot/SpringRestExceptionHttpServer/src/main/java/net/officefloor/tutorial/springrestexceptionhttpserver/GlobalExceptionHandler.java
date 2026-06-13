package net.officefloor.tutorial.springrestexceptionhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class GlobalExceptionHandler {
    public void handle(@Parameter EscalationException ex, ObjectResponse<String> response) {
        response.send("Escalation handled: " + ex.getMessage());
    }
}
// END SNIPPET: tutorial
