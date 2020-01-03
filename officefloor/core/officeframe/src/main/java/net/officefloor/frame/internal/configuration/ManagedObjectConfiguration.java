package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Configuration of a {@link ProcessState} or {@link ThreadState} bound
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectConfiguration<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link ManagedObject} registered within the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link ManagedObject} registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains name of the {@link ManagedObject} bound to either
	 * {@link ProcessState} or {@link ThreadState}.
	 * 
	 * @return Name of the {@link ManagedObject} bound to either
	 *         {@link ProcessState} or {@link ThreadState}.
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