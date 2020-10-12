package net.officefloor.frame.api.managedobject.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Enhances the {@link ManagedFunction} provided by a
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionEnhancer {

	/**
	 * Enhances the {@link ManagedFunction} provided by a
	 * {@link ManagedObjectSource}.
	 * 
	 * @param context {@link ManagedObjectFunctionEnhancerContext}.
	 */
	void enhanceFunction(ManagedObjectFunctionEnhancerContext context);

}