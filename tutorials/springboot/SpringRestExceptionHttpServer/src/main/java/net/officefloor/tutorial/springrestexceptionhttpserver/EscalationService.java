package net.officefloor.tutorial.springrestexceptionhttpserver;

// START SNIPPET: tutorial
public class EscalationService {
    public void service() throws EscalationException {
        throw new EscalationException("thrown");
    }
}
// END SNIPPET: tutorial
