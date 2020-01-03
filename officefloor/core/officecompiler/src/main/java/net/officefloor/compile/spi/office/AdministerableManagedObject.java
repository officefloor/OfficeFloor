package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} that may be administered by an
 * {@link OfficeAdministration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministerableManagedObject {

	/**
	 * Obtains the name of this {@link AdministerableManagedObject}.
	 * 
	 * @return Name of this {@link AdministerableManagedObject}.
	 */
	String getAdministerableManagedObjectName();
}