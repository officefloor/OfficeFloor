package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link Governance} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeGovernance extends PropertyConfigurable, OfficeResponsibility {

	/**
	 * Obtains the name of this {@link OfficeGovernance}.
	 * 
	 * @return Name of this {@link OfficeGovernance}.
	 */
	String getOfficeGovernanceName();

	/**
	 * Governs the {@link GovernerableManagedObject}.
	 * 
	 * @param managedObject {@link GovernerableManagedObject} to be governed.
	 */
	void governManagedObject(GovernerableManagedObject managedObject);

	/**
	 * Enables auto-wiring the {@link GovernerableManagedObject} instances.
	 */
	void enableAutoWireExtensions();
}