package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Check that the {@link ManagedObject} is ready.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectReadyCheck {

	/**
	 * Obtains the {@link FunctionState} to use in an {@link AssetLatch} if not
	 * ready.
	 * 
	 * @return {@link FunctionState} to use in an {@link AssetLatch} if not
	 *         ready.
	 */
	FunctionState getLatchFunction();

	/**
	 * Obtains the {@link ManagedFunctionContainer} to access dependent
	 * {@link ManagedObject} instances.
	 * 
	 * @return {@link ManagedFunctionContainer} to access dependent
	 *         {@link ManagedObject} instances.
	 */
	ManagedFunctionContainer getManagedFunctionContainer();

	/**
	 * Flags that a {@link ManagedObject} or one of its dependency
	 * {@link ManagedObject} instances is not ready.
	 * 
	 * @return {@link FunctionState} to flag the {@link ManagedObject} as not
	 *         ready.
	 */
	FunctionState setNotReady();

}