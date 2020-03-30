package net.officefloor.tutorial.warhttpserver;

import net.officefloor.tutorial.warapp.ServletDependency;

/**
 * {@link ServletDependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectServletDependency extends ServletDependency {

	@Override
	public String getMessage() {
		return "INJECT";
	}

}