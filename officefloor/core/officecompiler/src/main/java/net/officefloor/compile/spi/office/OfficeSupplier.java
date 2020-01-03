package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Supplies {@link OfficeManagedObjectSource} instances within the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSupplier extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeSupplier}.
	 * 
	 * @return Name of this {@link OfficeSupplier}.
	 */
	String getOfficeSupplierName();

	/**
	 * Obtains the {@link OfficeSupplierThreadLocal}.
	 * 
	 * @param qualifier Qualifier of the required {@link ManagedObject}. May be
	 *                  <code>null</code> to match only on type.
	 * @param type      Type of object required for the
	 *                  {@link OfficeSupplierThreadLocal}.
	 * @return {@link OfficeSupplierThreadLocal}.
	 */
	OfficeSupplierThreadLocal getOfficeSupplierThreadLocal(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName Name of the {@link OfficeManagedObjectSource}.
	 * @param qualifier               Qualifier on the object type. May be
	 *                                <code>null</code> to match only on type.
	 * @param type                    Type of object required from the
	 *                                {@link OfficeSupplier}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource getOfficeManagedObjectSource(String managedObjectSourceName, String qualifier,
			String type);

}