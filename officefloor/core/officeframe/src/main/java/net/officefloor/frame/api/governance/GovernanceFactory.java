package net.officefloor.frame.api.governance;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Factory for the creation of the {@link Governance}.
 * 
 * @param <E>
 *            Extension interface type for the {@link ManagedObject} instances
 *            to be under this {@link Governance}.
 * @param <F>
 *            {@link Flow} keys for the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceFactory<E, F extends Enum<F>> {

	/**
	 * Creates the {@link Governance}.
	 * 
	 * @return {@link Governance}.
	 * @throws Throwable
	 *             If fails to create the {@link Governance}.
	 */
	Governance<E, F> createGovernance() throws Throwable;

}