package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Factory for the creation of an {@link Administration}.
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
public interface AdministrationFactory<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Creates the {@link Administration}.
	 * 
	 * @return {@link Administration}.
	 * @throws Throwable
	 *             If fails to create {@link Administration}.
	 */
	Administration<E, F, G> createAdministration() throws Throwable;

}