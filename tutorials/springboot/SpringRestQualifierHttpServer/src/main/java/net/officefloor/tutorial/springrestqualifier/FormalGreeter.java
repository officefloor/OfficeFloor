package net.officefloor.tutorial.springrestqualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

// START SNIPPET: tutorial
@Component
@Qualifier("formal")
public class FormalGreeter implements Greeter {

	@Override
	public String greet(String name) {
		return "Good day, " + name + ".";
	}
}
// END SNIPPET: tutorial
