package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.administration.source.AdministrationSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link AdministrationSource}
 * {@link Class} alias by including the extension {@link AdministrationSource}
 * jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addAdministrationSourceAlias(String, Class)} will
 * be invoked for each found {@link AdministrationSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceService<E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> {

	/**
	 * Obtains the alias for the {@link AdministrationSource} {@link Class}.
	 * 
	 * @return Alias for the {@link AdministrationSource} {@link Class}.
	 */
	String getAdministrationSourceAlias();

	/**
	 * Obtains the {@link AdministrationSource} {@link Class}.
	 * 
	 * @return {@link AdministrationSource} {@link Class}.
	 */
	Class<S> getAdministrationSourceClass();

}