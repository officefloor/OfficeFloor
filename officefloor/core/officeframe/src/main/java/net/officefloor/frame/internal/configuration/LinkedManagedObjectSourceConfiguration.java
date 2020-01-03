package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Configuration linking in a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedManagedObjectSourceConfiguration {

	/**
	 * Obtains the name of the {@link OfficeFloor} {@link ManagedObjectSource}
	 * instance.
	 * 
	 * @return Name of the {@link OfficeFloor} {@link ManagedObjectSource}
	 *         instance.
	 */
	String getOfficeFloorManagedObjectSourceName();

	/**
	 * Obtains the name that the {@link ManagedObject} is registered within the
	 * {@link Office}.
	 * 
	 * @return Name that the {@link ManagedObject} is registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

}