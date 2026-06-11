package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.ObjectResponse;

/**
 * Using {@link Out} and {@link In} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class InLogic {

	public static void useValues(In<Person> person, @Description In<String> description,
			ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse(person.get(), description.get()));
	}
}
// END SNIPPET: tutorial