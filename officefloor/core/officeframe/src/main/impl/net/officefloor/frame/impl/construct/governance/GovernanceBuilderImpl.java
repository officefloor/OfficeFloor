package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.impl.construct.function.AbstractFunctionBuilder;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<E, F extends Enum<F>> extends AbstractFunctionBuilder<F>
		implements GovernanceBuilder<F>, GovernanceConfiguration<E, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionType;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super E, F> governanceFactory;

	/**
	 * {@link AsynchronousFlow} timeout.
	 */
	private long asynchronousFlowTimeout = -1;

	/**
	 * Initiate.
	 * 
	 * @param governanceName    Name of the {@link Governance}.
	 * @param extensionType     Extension interface.
	 * @param governanceFactory {@link GovernanceFactory}.
	 */
	public GovernanceBuilderImpl(String governanceName, Class<E> extensionType,
			GovernanceFactory<? super E, F> governanceFactory) {
		this.governanceName = governanceName;
		this.extensionType = extensionType;
		this.governanceFactory = governanceFactory;
	}

	/*
	 * =============== GovernanceBuilder ====================
	 */

	@Override
	public void setAsynchronousFlowTimeout(long timeout) {
		this.asynchronousFlowTimeout = timeout;
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super E, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<E> getExtensionType() {
		return this.extensionType;
	}

	@Override
	public long getAsynchronousFlowTimeout() {
		return this.asynchronousFlowTimeout;
	}

}