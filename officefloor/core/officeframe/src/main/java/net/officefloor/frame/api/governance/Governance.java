package net.officefloor.frame.api.governance;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Provides {@link Governance} over the {@link ManagedObject} instances.
 * <p>
 * The extension interface of the {@link ManagedObject} is used to provide the
 * {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Governance<E, F extends Enum<F>> {

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 * 
	 * @param managedObjectExtension
	 *            Extension of the {@link ManagedObject} to enable
	 *            {@link Governance}.
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to govern the {@link ManagedObject}.
	 */
	void governManagedObject(E managedObjectExtension, GovernanceContext<F> context) throws Throwable;

	/**
	 * Enforces the {@link Governance} of the {@link ManagedObject} instances
	 * under {@link Governance}.
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to enforce {@link Governance}.
	 */
	void enforceGovernance(GovernanceContext<F> context) throws Throwable;

	/**
	 * Disregard {@link Governance} of the {@link ManagedObject} instances.
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Throwable
	 *             If fails to disregard {@link Governance}.
	 */
	void disregardGovernance(GovernanceContext<F> context) throws Throwable;

}