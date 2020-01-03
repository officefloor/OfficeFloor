package net.officefloor.compile.supplier;

import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalType extends OptionalThreadLocalReceiver {

	/**
	 * Obtains the type of {@link Object} required.
	 * 
	 * @return Type of {@link Object} required.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the possible qualifier for the required {@link ManagedObject}.
	 * 
	 * @return Qualifier for the required {@link ManagedObject}. May be
	 *         <code>null</code>.
	 */
	String getQualifier();

}