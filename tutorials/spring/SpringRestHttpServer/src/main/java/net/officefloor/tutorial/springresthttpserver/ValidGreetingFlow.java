package net.officefloor.tutorial.springresthttpserver;

// START SNIPPET: tutorial
@FunctionalInterface
public interface ValidGreetingFlow {

	void withRequest(GreetingRequest request);
}
// END SNIPPET: tutorial
