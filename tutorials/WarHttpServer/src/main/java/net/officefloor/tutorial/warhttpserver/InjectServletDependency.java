package net.officefloor.tutorial.warhttpserver;

import net.officefloor.tutorial.warapp.ServletDependency;

/**
 * {@link ServletDependency}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class InjectServletDependency extends ServletDependency {

	@Override
	public String getMessage() {
		return "INJECT";
	}
}
// END SNIPPET: tutorial