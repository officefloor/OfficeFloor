package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * {@link ManagedObjectPool} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectPool extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeManagedObjectPool}.
	 * 
	 * @return Name of this {@link OfficeManagedObjectPool}.
	 */
	String getOfficeManagedObjectPoolName();

}