package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationConfiguration<E, F extends Enum<F>, G extends Enum<G>> extends FunctionConfiguration<F> {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface.
	 * 
	 * @return Extension interface.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the names of the {@link ManagedObject} instances to be administered.
	 * 
	 * @return Names of the {@link ManagedObject} instances to be administered.
	 */
	String[] getAdministeredManagedObjectNames();

	/**
	 * Obtains the configuration for the linked {@link Governance}.
	 * 
	 * @return {@link AdministrationGovernanceConfiguration} specifying the linked
	 *         {@link Governance}.
	 */
	AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration();

	/**
	 * Obtains the timeout for any {@link AsynchronousFlow} instigated.
	 * 
	 * @return Timeout for any {@link AsynchronousFlow} instigated.
	 */
	long getAsynchronousFlowTimeout();

}