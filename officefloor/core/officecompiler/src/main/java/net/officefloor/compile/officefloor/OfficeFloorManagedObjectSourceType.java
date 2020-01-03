package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a {@link ManagedObjectSource} available to be
 * configured in the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObjectSourceType {

	/**
	 * Obtains the name of the {@link ManagedObjectSource} within the
	 * {@link OfficeFloor}.
	 * 
	 * @return Name of the {@link ManagedObjectSource} within the
	 *         {@link OfficeFloor}.
	 */
	String getOfficeFloorManagedObjectSourceName();

	/**
	 * Obtains the {@link OfficeFloorManagedObjectSourcePropertyType} instances
	 * identify the {@link Property} instances that may be configured for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link OfficeFloorManagedObjectSourcePropertyType} instances.
	 */
	OfficeFloorManagedObjectSourcePropertyType[] getOfficeFloorManagedObjectSourcePropertyTypes();

}