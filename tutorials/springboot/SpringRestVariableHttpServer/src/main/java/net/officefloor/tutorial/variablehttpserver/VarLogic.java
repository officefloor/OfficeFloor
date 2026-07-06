package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.Var;

// START SNIPPET: tutorial
public class VarLogic {

	public static void setValues(Var<Person> person, @Description Var<String> description) {
		person.set(new Person("Daniel", "Sagenschneider"));
		description.set("Need to watch his code!");
	}
}
// END SNIPPET: tutorial