package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * Provides additional means to link {@link ManagedObjectFunctionDependency}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputDependencyMappingBuilder extends ThreadDependencyMappingBuilder {

	/**
	 * Specifies the {@link ManagedObject} for the
	 * {@link ManagedObjectFunctionDependency}.
	 * 
	 * @param functionObjectName      Name of the
	 *                                {@link ManagedObjectFunctionDependency}.
	 * @param scopedManagedObjectName Name of the {@link ManagedObject}.
	 */
	void mapFunctionDependency(String functionObjectName, String scopedManagedObjectName);

}