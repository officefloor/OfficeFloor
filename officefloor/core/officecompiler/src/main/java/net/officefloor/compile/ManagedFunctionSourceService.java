package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link ManagedFunctionSource}
 * {@link Class} alias by including the extension {@link ManagedFunctionSource}
 * jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addManagedFunctionSourceAlias(String, Class)} will
 * be invoked for each found {@link ManagedFunctionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionSourceService<S extends ManagedFunctionSource> {

	/**
	 * Obtains the alias for the {@link ManagedFunctionSource} {@link Class}.
	 * 
	 * @return Alias for the {@link ManagedFunctionSource} {@link Class}.
	 */
	String getManagedFunctionSourceAlias();

	/**
	 * Obtains the {@link ManagedFunctionSource} {@link Class}.
	 * 
	 * @return {@link ManagedFunctionSource} {@link Class}.
	 */
	Class<S> getManagedFunctionSourceClass();

}