package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;

/**
 * Augments the {@link ManagedFunction} instances within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionAugmentor {

	/**
	 * Augments the {@link ManagedFunction}.
	 * 
	 * @param context {@link ManagedFunctionAugmentorContext}.
	 */
	void augmentManagedFunction(ManagedFunctionAugmentorContext context);

}