package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <p>
 * Enables enhancing the {@link Office}.
 * <p>
 * This enables:
 * <ol>
 * <li>linking in the {@link ManagedFunction} instances created by the
 * {@link ManagedObjectSource} to other {@link ManagedFunction} instances within
 * the {@link Office}</li>
 * <li>linking the {@link Flow} instances instigated by the
 * {@link ManagedObjectSource} to a {@link ManagedFunction} instances within the
 * {@link Office}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancer {

	/**
	 * Enhances the {@link Office}.
	 * 
	 * @param context
	 *            {@link OfficeEnhancerContext}.
	 */
	void enhanceOffice(OfficeEnhancerContext context);

}