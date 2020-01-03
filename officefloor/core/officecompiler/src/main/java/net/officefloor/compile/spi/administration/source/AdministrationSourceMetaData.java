package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link AdministrationFactory} to create the
	 * {@link Administration} for this {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationFactory}
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the list of {@link AdministrationFlowMetaData} instances should
	 * this {@link Administration} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link Administration}.
	 */
	AdministrationFlowMetaData<F>[] getFlowMetaData();

	/**
	 * Obtains the list of {@link AdministrationEscalationMetaData} instances
	 * from this {@link Administration}.
	 * 
	 * @return Meta-data of {@link Escalation} instances instigated by this
	 *         {@link Administration}.
	 */
	AdministrationEscalationMetaData[] getEscalationMetaData();

	/**
	 * Obtains the list of {@link AdministrationGovernanceMetaData} instances
	 * should this {@link Administration} manually managed {@link Governance}.
	 * 
	 * @return Meta-data of {@link Governance} used by this
	 *         {@link Administration}.
	 */
	AdministrationGovernanceMetaData<G>[] getGovernanceMetaData();

}