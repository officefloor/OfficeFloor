package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.function.FunctionFlowContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context in which the {@link Administration} executes.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationContext<E extends Object, F extends Enum<F>, G extends Enum<G>>
		extends FunctionFlowContext<F> {

	/**
	 * Obtains the particular extensions.
	 * 
	 * @return Extension for the {@link ManagedObject} instances to be administered.
	 */
	E[] getExtensions();

	/**
	 * Obtains the {@link GovernanceManager} for the particular key.
	 * 
	 * @param key Key identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(G key);

	/**
	 * Obtains the {@link GovernanceManager} for the index.
	 * 
	 * @param governanceIndex Index identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(int governanceIndex);

}