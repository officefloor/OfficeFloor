package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} available to the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionManagedObject {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectType} for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectType} for the {@link ManagedObject}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Obtains the {@link ExecutionManagedObject} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param dependencyType
	 *            {@link ManagedObjectDependencyType}.
	 * @return {@link ExecutionManagedObject} for the
	 *         {@link ManagedObjectDependencyType}.
	 */
	ExecutionManagedObject getManagedObject(ManagedObjectDependencyType<?> dependencyType);

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param flowType
	 *            {@link ManagedObjectFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link ManagedObjectFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(ManagedObjectFlowType<?> flowType);

}