package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;

/**
 * Using {@link Var} and {@link Val} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class VarLogic {

	@Next("use")
	public void setValues(Var<Person> person, @Description Var<String> description) {
		person.set(new Person("Daniel", "Sagenschneider"));
		description.set("Need to watch his code!");
	}
}
// END SNIPPET: tutorial