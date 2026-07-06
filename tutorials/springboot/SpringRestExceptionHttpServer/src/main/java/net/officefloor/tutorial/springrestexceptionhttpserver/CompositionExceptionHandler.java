package net.officefloor.tutorial.springrestexceptionhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class CompositionExceptionHandler {
    public void handle(@Parameter MockException ex, ObjectResponse<String> response) {
        response.send("Composition handled: " + ex.getMessage());
    }
}
// END SNIPPET: tutorial
