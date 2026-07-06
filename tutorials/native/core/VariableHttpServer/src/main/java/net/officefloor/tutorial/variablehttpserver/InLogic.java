package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.In;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class InLogic {

	public static void useValues(In<Person> person, @Description In<String> description,
			ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse(person.get(), description.get()));
	}
}
// END SNIPPET: tutorial