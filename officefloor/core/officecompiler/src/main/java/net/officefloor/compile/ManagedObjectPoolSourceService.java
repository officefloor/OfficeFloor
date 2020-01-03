package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link ManagedObjectPoolSource}
 * {@link Class} alias by including the extension {@link ManagedObjectPoolSource}
 * jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addManagedObjectPoolSourceAlias(String, Class)} will
 * be invoked for each found {@link ManagedObjectPoolSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSourceService<S extends ManagedObjectPoolSource> {

	/**
	 * Obtains the alias for the {@link ManagedObjectPoolSource} {@link Class}.
	 * 
	 * @return Alias for the {@link ManagedObjectPoolSource} {@link Class}.
	 */
	String getManagedObjectPoolSourceAlias();

	/**
	 * Obtains the {@link ManagedObjectPoolSource} {@link Class}.
	 * 
	 * @return {@link ManagedObjectPoolSource} {@link Class}.
	 */
	Class<S> getManagedObjectPoolSourceClass();

}