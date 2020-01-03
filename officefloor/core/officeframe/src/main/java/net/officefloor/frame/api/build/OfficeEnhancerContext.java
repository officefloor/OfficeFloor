package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link OfficeEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancerContext {

	/**
	 * Obtains the {@link FlowBuilder} registered under the input
	 * {@link ManagedFunction} name.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String functionName);

	/**
	 * Obtains the {@link FlowBuilder} registered by the
	 * {@link ManagedObjectSource} under the input {@link ManagedFunction} name.
	 * 
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} name registered with the
	 *            {@link OfficeFloorBuilder}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String managedObjectSourceName, String functionName);

}