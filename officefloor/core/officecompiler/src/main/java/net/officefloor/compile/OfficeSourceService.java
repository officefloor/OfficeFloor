package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.office.source.OfficeSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link OfficeSource}
 * {@link Class} alias by including the extension {@link OfficeSource} jar on
 * the class path.
 * <p>
 * {@link OfficeFloorCompiler#addOfficeSourceAlias(String, Class)} will be
 * invoked for each found {@link OfficeSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSourceService<S extends OfficeSource> {

	/**
	 * Obtains the alias for the {@link OfficeSource} {@link Class}.
	 * 
	 * @return Alias for the {@link OfficeSource} {@link Class}.
	 */
	String getOfficeSourceAlias();

	/**
	 * Obtains the {@link OfficeSource} {@link Class}.
	 * 
	 * @return {@link OfficeSource} {@link Class}.
	 */
	Class<S> getOfficeSourceClass();

}