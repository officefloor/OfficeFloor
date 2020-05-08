package net.officefloor.jaxrs;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link Dependency} for the {@link DependencyResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceDependency {

	private final String message;

	public ResourceDependency(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}