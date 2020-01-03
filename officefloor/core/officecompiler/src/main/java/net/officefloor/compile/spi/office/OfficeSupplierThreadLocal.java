package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SupplierThreadLocal} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSupplierThreadLocal extends OfficeDependencyRequireNode {

	/**
	 * Obtains the name of this {@link OfficeSupplierThreadLocal}.
	 * 
	 * @return Name of this {@link OfficeSupplierThreadLocal}.
	 */
	String getOfficeSupplierThreadLocalName();

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