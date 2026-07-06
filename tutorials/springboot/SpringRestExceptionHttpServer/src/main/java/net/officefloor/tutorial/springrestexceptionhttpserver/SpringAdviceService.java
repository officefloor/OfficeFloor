package net.officefloor.tutorial.springrestexceptionhttpserver;

// START SNIPPET: tutorial
public class SpringAdviceService {
    public void service() throws MockException {
        throw new MockException("thrown");
    }
}
// END SNIPPET: tutorial
