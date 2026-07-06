package net.officefloor.tutorial.springrestqualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

// START SNIPPET: tutorial
@Component
@Qualifier("casual")
public class CasualGreeter implements Greeter {

	@Override
	public String greet(String name) {
		return "Hey " + name + "!";
	}
}
// END SNIPPET: tutorial
