package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link SupplierThreadLocal} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSupplierThreadLocal extends OfficeFloorDependencyRequireNode {

	/**
	 * Obtains the name of this {@link OfficeFloorSupplierThreadLocal}.
	 * 
	 * @return Name of this {@link OfficeFloorSupplierThreadLocal}.
	 */
	String getOfficeFloorSupplierThreadLocalName();

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the required type.
	 * 
	 * @return Required type.
	 */
	String getType();

}