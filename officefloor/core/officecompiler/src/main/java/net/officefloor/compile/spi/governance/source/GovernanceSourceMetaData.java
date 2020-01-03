package net.officefloor.compile.spi.governance.source;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceMetaData<I, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends I, F> getGovernanceFactory();

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be governed.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the list of {@link GovernanceFlowMetaData} instances should this
	 * {@link GovernanceSource} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link GovernanceSource}.
	 */
	GovernanceFlowMetaData<F>[] getFlowMetaData();

	/**
	 * Obtains the potential {@link Escalation} types from the
	 * {@link Governance}.
	 * 
	 * @return Potential {@link Escalation} types from the {@link Governance}.
	 */
	Class<?>[] getEscalationTypes();

}