package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration for the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceConfiguration<E, F extends Enum<F>> extends FunctionConfiguration<F> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? super E, F> getGovernanceFactory();

	/**
	 * Obtains the extension interface type for {@link ManagedObject} to provide to
	 * enable {@link Governance}.
	 * 
	 * @return Extension interface type for {@link ManagedObject} to provide to
	 *         enable {@link Governance}.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the timeout for any {@link AsynchronousFlow} instigated.
	 * 
	 * @return Timeout for any {@link AsynchronousFlow} instigated.
	 */
	long getAsynchronousFlowTimeout();

}