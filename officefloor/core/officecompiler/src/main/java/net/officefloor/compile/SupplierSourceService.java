package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.supplier.source.SupplierSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link SupplierSource}
 * {@link Class} alias by including the extension {@link SupplierSource} jar on
 * the class path.
 * <p>
 * {@link OfficeFloorCompiler#addSupplierSourceAlias(String, Class)} will be
 * invoked for each found {@link SupplierSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSourceService<S extends SupplierSource> {

	/**
	 * Obtains the alias for the {@link SupplierSource} {@link Class}.
	 * 
	 * @return Alias for the {@link SupplierSource} {@link Class}.
	 */
	String getSupplierSourceAlias();

	/**
	 * Obtains the {@link SupplierSource} {@link Class}.
	 * 
	 * @return {@link SupplierSource} {@link Class}.
	 */
	Class<S> getSupplierSourceClass();

}