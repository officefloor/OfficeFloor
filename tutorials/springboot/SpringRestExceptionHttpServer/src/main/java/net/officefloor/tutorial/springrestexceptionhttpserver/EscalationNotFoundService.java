package net.officefloor.tutorial.springrestexceptionhttpserver;

// START SNIPPET: tutorial
public class EscalationNotFoundService {
    public void service() throws EscalationNotFoundException {
        throw new EscalationNotFoundException("entity not found");
    }
}
// END SNIPPET: tutorial
