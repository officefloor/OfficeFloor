package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link Administration} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeAdministration extends PropertyConfigurable, OfficeResponsibility {

	/**
	 * Obtains the name of this {@link OfficeAdministration}.
	 * 
	 * @return Name of this {@link OfficeAdministration}.
	 */
	String getOfficeAdministrationName();

	/**
	 * Administers the {@link AdministerableManagedObject}.
	 * 
	 * @param managedObject {@link AdministerableManagedObject} to be administered.
	 */
	void administerManagedObject(AdministerableManagedObject managedObject);

	/**
	 * Enables auto-wiring the {@link AdministerableManagedObject} instances.
	 */
	void enableAutoWireExtensions();
}