package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a dependency required by a
 * {@link ManagedFunction} added by {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependencyType {

	/**
	 * Obtains the name of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Name of the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectName();

	/**
	 * Obtains the type of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Type of the {@link ManagedObjectFunctionDependency}.
	 */
	Class<?> getFunctionObjectType();

}