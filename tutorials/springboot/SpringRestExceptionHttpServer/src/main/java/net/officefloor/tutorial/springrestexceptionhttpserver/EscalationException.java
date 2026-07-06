package net.officefloor.tutorial.springrestexceptionhttpserver;

// START SNIPPET: tutorial
public class EscalationException extends Exception {

    public EscalationException(String message) {
        super(message);
    }
}
// END SNIPPET: tutorial
