package net.officefloor.compile.spi.supplier.source;


/**
 * Provides the specification of the Supplier to be loaded by the particular
 * {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSourceSpecification {

	/**
	 * Obtains the specification of the properties for the Supplier.
	 * 
	 * @return Property specification.
	 */
	SupplierSourceProperty[] getProperties();

}