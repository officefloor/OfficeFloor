package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.web.ObjectResponse;

/**
 * Using {@link Var} and {@link Val} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ValLogic {

	public void useValues(@Val Person person, @Description @Val String description,
			ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse(person, description));
	}
}
// END SNIPPET: tutorial