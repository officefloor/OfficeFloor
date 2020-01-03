package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;

/**
 * Builds the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceBuilder<F extends Enum<F>> extends FunctionBuilder<F> {

	/**
	 * Specifies the timeout to for {@link AsynchronousFlow} instances for this
	 * {@link Governance}.
	 *
	 * @param timeout Timeout.
	 */
	void setAsynchronousFlowTimeout(long timeout);

}