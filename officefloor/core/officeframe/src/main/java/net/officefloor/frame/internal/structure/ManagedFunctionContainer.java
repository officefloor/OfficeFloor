package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Container of {@link ManagedFunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContainer extends BlockState {

	/**
	 * Obtains the {@link Flow} containing this {@link ManagedObjectContainer}.
	 * 
	 * @return {@link Flow} containing this {@link ManagedObjectContainer}.
	 */
	Flow getFlow();

	/**
	 * Obtains the {@link ManagedObjectContainer} bound to this
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @param index Index of the {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer} bound to this
	 *         {@link ManagedFunctionContainer}.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * <p>
	 * Creates a {@link ManagedFunctionInterest} in this
	 * {@link ManagedFunctionContainer}.
	 * <p>
	 * The {@link ManagedFunctionContainer} will not unload its
	 * {@link ManagedFunction} bound {@link ManagedObject} instances until all
	 * registered {@link ManagedFunctionInterest} instances have been unregistered.
	 * 
	 * @return New {@link ManagedFunctionInterest} in this
	 *         {@link ManagedFunctionContainer}.
	 */
	ManagedFunctionInterest createInterest();

}