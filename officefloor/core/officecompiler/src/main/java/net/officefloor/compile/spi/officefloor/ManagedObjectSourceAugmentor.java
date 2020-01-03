package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Augments the {@link ManagedObjectSource} instances within the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceAugmentor {

	/**
	 * Augments the {@link ManagedObjectSource}.
	 * 
	 * @param context {@link ManagedObjectSourceAugmentorContext}.
	 */
	void augmentManagedObjectSource(ManagedObjectSourceAugmentorContext context);

}