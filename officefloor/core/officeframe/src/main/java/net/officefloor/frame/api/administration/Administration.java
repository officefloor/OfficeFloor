package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides administration of the {@link ManagedObject} instances.
 * 
 * @param <E>
 *            Extension interface used to administer the {@link ManagedObject}
 *            instances.
 * @param <F>
 *            {@link Flow} keys for invoked {@link Flow} instances from this
 *            {@link Administration}.
 * @param <G>
 *            {@link Governance} keys identifying the {@link Governance} that
 *            may be under {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Administration<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Administers the {@link ManagedObject} instances.
	 * 
	 * @param context
	 *            {@link AdministrationContext}.
	 * @throws Throwable
	 *             If fails to do duty.
	 */
	void administer(AdministrationContext<E, F, G> context) throws Throwable;

}