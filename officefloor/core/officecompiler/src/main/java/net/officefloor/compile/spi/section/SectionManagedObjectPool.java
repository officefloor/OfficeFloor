package net.officefloor.compile.spi.section;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * {@link ManagedObjectPool} within the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionManagedObjectPool extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SectionManagedObjectPool}.
	 * 
	 * @return Name of this {@link SectionManagedObjectPool}.
	 */
	String getSectionManagedObjectPoolName();

}