package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} that may be governed by an {@link OfficeGovernance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernerableManagedObject {

	/**
	 * Obtains the name of this {@link GovernerableManagedObject}.
	 * 
	 * @return Name of this {@link GovernerableManagedObject}.
	 */
	String getGovernerableManagedObjectName();
}