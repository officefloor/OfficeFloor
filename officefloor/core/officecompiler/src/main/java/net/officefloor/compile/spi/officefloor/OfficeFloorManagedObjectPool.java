package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * {@link ManagedObjectPool} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObjectPool extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObjectPool}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObjectPool}.
	 */
	String getOfficeFloorManagedObjectPoolName();

}