package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * Configuration of {@link ManagedObjectFunctionDependency} to another
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependencyConfiguration {

	/**
	 * Obtains name of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Name of the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectName();

	/**
	 * Obtains the name of the {@link ManagedObject} providing the dependency.
	 * 
	 * @return Name of the {@link ManagedObject} providing the dependency.
	 */
	String getScopeManagedObjectName();

}