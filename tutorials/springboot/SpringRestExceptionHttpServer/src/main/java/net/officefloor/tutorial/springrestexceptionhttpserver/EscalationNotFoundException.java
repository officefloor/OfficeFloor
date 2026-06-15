package net.officefloor.tutorial.springrestexceptionhttpserver;

// START SNIPPET: tutorial
public class EscalationNotFoundException extends Exception {

    public EscalationNotFoundException(String message) {
        super(message);
    }
}
// END SNIPPET: tutorial
