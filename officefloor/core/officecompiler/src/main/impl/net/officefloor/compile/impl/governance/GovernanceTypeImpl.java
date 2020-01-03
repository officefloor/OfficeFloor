package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceTypeImpl<I, F extends Enum<F>> implements
		GovernanceType<I, F> {

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? extends I, F> governanceFactory;

	/**
	 * Extension interface.
	 */
	private final Class<I> extensionInterface;

	/**
	 * {@link GovernanceFlowType} instances.
	 */
	private final GovernanceFlowType<F>[] flowTypes;

	/**
	 * {@link GovernanceEscalationType} instances.
	 */
	private final GovernanceEscalationType[] escalationTypes;

	/**
	 * Initiate.
	 * 
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param flowTypes
	 *            {@link GovernanceFlowType} instances.
	 * @param escalationTypes
	 *            {@link GovernanceEscalationType} instances.
	 */
	public GovernanceTypeImpl(
			GovernanceFactory<? extends I, F> governanceFactory,
			Class<I> extensionInterface, GovernanceFlowType<F>[] flowTypes,
			GovernanceEscalationType[] escalationTypes) {
		this.governanceFactory = governanceFactory;
		this.extensionInterface = extensionInterface;
		this.flowTypes = flowTypes;
		this.escalationTypes = escalationTypes;
	}

	/*
	 * ======================== GovernanceType ======================
	 */

	@Override
	public GovernanceFactory<? extends I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<I> getExtensionType() {
		return this.extensionInterface;
	}

	@Override
	public GovernanceFlowType<F>[] getFlowTypes() {
		return this.flowTypes;
	}

	@Override
	public GovernanceEscalationType[] getEscalationTypes() {
		return this.escalationTypes;
	}

}