package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} to be configured from the {@link ManagedObjectSource} (invoked
 * via {@link ManagedObjectExecuteContext}).
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceFlow {

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction}
	 * of the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkFunction(String functionName);

}