package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;

/**
 * Using {@link Out} and {@link In} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class OutLogic {

	@Next("use")
	public static void setValues(Out<Person> person, @Description Out<String> description) {
		person.set(new Person("Daniel", "Sagenschneider"));
		description.set("Need to watch his code!");
	}
}
// END SNIPPET: tutorial