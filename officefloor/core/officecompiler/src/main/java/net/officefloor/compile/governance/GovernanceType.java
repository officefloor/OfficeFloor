package net.officefloor.compile.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceType<E, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends E, F> getGovernanceFactory();

	/**
	 * Obtains the extension type that the {@link ManagedObject} instances are to
	 * provide to be enable {@link Governance} over them.
	 * 
	 * @return Extension type that the {@link ManagedObject} instances are to
	 *         provide to be enable {@link Governance} over them.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link GovernanceFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link GovernanceActivity}.
	 */
	GovernanceFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link GovernanceEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 */
	GovernanceEscalationType[] getEscalationTypes();

}