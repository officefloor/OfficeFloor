package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} that a {@link OfficeSectionFunction} or {@link ManagedObject}
 * depends upon.
 * 
 * @author Daniel Sagenschneider
 */
public interface DependentManagedObject {

	/**
	 * Obtains the name of this {@link DependentManagedObject}.
	 * 
	 * @return Name of this {@link DependentManagedObject}.
	 */
	String getDependentManagedObjectName();

}