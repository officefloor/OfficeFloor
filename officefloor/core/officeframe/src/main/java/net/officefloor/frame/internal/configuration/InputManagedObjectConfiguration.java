package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Configuration of a {@link ManagedObject} input into a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputManagedObjectConfiguration<O extends Enum<O>> {

	/**
	 * Obtains name the input {@link ManagedObject} is bound to within the
	 * {@link ProcessState}.
	 * 
	 * @return Name the input {@link ManagedObject} is bound to within the
	 *         {@link ProcessState}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<O>[] getDependencyConfiguration();

	/**
	 * Obtains the listing of {@link ManagedObjectGovernanceConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectGovernanceConfiguration} instances.
	 */
	ManagedObjectGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link Administration} to be done before the
	 * {@link ManagedObject} is loaded.
	 * 
	 * @return Listing of the {@link Administration} to be done before the
	 *         {@link ManagedObject} is loaded.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPreLoadAdministration();

	/**
	 * Obtains the {@link ThreadLocalConfiguration}.
	 * 
	 * @return {@link ThreadLocalConfiguration} or <code>null</code> if not bound to
	 *         {@link Thread}.
	 */
	ThreadLocalConfiguration getThreadLocalConfiguration();

}