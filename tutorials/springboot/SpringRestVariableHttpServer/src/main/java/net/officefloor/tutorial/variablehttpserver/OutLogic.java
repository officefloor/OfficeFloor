package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.Out;

/**
 * Using {@link Out} for variables.
 *
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class OutLogic {

	public static void setValues(Out<Person> person, @Description Out<String> description) {
		person.set(new Person("Daniel", "Sagenschneider"));
		description.set("Need to watch his code!");
	}
}
// END SNIPPET: tutorial