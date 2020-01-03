package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link ManagedObjectSource}
 * {@link Class} alias by including the extension {@link ManagedObjectSource}
 * jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addManagedObjectSourceAlias(String, Class)} will
 * be invoked for each found {@link ManagedObjectSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceService<D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> {

	/**
	 * Obtains the alias for the {@link ManagedObjectSource} {@link Class}.
	 * 
	 * @return Alias for the {@link ManagedObjectSource} {@link Class}.
	 */
	String getManagedObjectSourceAlias();

	/**
	 * Obtains the {@link ManagedObjectSource} {@link Class}.
	 * 
	 * @return {@link ManagedObjectSource} {@link Class}.
	 */
	Class<S> getManagedObjectSourceClass();

}