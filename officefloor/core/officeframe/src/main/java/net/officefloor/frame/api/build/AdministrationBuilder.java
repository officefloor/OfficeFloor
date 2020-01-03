package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Enables building an {@link Administration}.
 * 
 * @param <F> {@link Flow} key type.
 * @param <G> {@link Governance} key type.
 * @author Daniel Sagenschneider
 */
public interface AdministrationBuilder<F extends Enum<F>, G extends Enum<G>> extends FunctionBuilder<F> {

	/**
	 * Flags for the {@link Administration} to administer the referenced
	 * {@link ManagedObject}. This may be called more than once to register more
	 * than one {@link ManagedObject} to be administered by this
	 * {@link Administration}.
	 * 
	 * @param scopeManagedObjectName Name of the {@link ManagedObject} within the
	 *                               scope this {@link Administration} is being
	 *                               added.
	 */
	void administerManagedObject(String scopeManagedObjectName);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param key            Key to identify the {@link Governance}.
	 * @param governanceName Name of the {@link Governance}.
	 */
	void linkGovernance(G key, String governanceName);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param governanceIndex Index to identify the {@link Governance}.
	 * @param governanceName  Name of the {@link Governance}.
	 */
	void linkGovernance(int governanceIndex, String governanceName);

	/**
	 * Specifies the timeout to for {@link AsynchronousFlow} instances for this
	 * {@link Administration}.
	 *
	 * @param timeout Timeout.
	 */
	void setAsynchronousFlowTimeout(long timeout);

}