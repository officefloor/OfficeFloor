package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Interest in a {@link ManagedFunctionContainer}.
 * <p>
 * The {@link ManagedFunctionContainer} will not unload its
 * {@link ManagedFunction} bound {@link ManagedObject} instances until all
 * {@link ManagedFunctionInterest} instances have been unregistered.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionInterest {

	/**
	 * Registers interest in the {@link ManagedFunctionContainer}.
	 * 
	 * @return {@link FunctionState} to register insterest in the
	 *         {@link ManagedFunctionContainer}.
	 */
	FunctionState registerInterest();

	/**
	 * Unregisters interest in the {@link ManagedFunctionContainer}.
	 * 
	 * @return {@link FunctionState} to unregister interest in the
	 *         {@link ManagedFunctionContainer}.
	 */
	FunctionState unregisterInterest();

}