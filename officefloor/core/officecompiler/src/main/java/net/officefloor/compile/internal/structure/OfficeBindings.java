package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Bindings to the {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeBindings {

	/**
	 * Builds the {@link ManagedObjectSourceNode} into the {@link Office}.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode}.
	 */
	void buildManagedObjectSourceIntoOffice(ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Builds the {@link BoundManagedObjectNode} into the {@link Office}.
	 * 
	 * @param managedObjectNode
	 *            {@link BoundManagedObjectNode}.
	 */
	void buildManagedObjectIntoOffice(BoundManagedObjectNode managedObjectNode);

	/**
	 * Builds the {@link InputManagedObjectNode} into the {@link Office}.
	 * 
	 * @param inputManagedObjectNode
	 *            {@link InputManagedObjectNode}.
	 */
	void buildInputManagedObjectIntoOffice(InputManagedObjectNode inputManagedObjectNode);

	/**
	 * Builds the {@link ManagedFunctionNode} into the {@link Office}.
	 * 
	 * @param managedFunctionNode
	 *            {@link ManagedFunctionNode}.
	 */
	void buildManagedFunctionIntoOffice(ManagedFunctionNode managedFunctionNode);

}