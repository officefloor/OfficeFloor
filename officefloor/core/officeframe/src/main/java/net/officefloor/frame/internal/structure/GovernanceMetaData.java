package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * Meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceMetaData<E, F extends Enum<F>> extends ManagedFunctionLogicMetaData {

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
	 * Creates the {@link GovernanceContainer}.
	 * 
	 * @param threadState     {@link ThreadState}.
	 * @param governanceIndex Index of the {@link Governance} within the
	 *                        {@link ThreadState}.
	 * @return {@link GovernanceContainer}.
	 */
	GovernanceContainer<E> createGovernanceContainer(ThreadState threadState, int governanceIndex);

	/**
	 * Creates the {@link ManagedFunctionContainer} for the
	 * {@link GovernanceActivity}.
	 * 
	 * @param activity {@link GovernanceActivity}.
	 * @return {@link ManagedFunctionLogic} for the {@link GovernanceActivity}.
	 */
	ManagedFunctionLogic createGovernanceFunctionLogic(GovernanceActivity<F> activity);

}