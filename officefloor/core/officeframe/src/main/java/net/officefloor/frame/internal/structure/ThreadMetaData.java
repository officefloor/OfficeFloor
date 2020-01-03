package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * Meta-data for the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadMetaData {

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ThreadState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 *         bound to the {@link ThreadState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link GovernanceMetaData} of the possible {@link Governance}
	 * within this {@link ThreadState}.
	 * 
	 * @return {@link GovernanceMetaData} instances.
	 */
	GovernanceMetaData<?, ?>[] getGovernanceMetaData();

	/**
	 * <p>
	 * Obtains the maximum {@link FunctionState} chain length for this
	 * {@link ThreadState}.
	 * <p>
	 * Once the {@link FunctionState} chain has reached this length, it will be
	 * broken. (spawned in another {@link Thread}). This avoids
	 * {@link StackOverflowError} issues in {@link FunctionState} chain being too
	 * large.
	 * 
	 * @return Maximum {@link FunctionState} chain length for this
	 *         {@link ThreadState}.
	 */
	int getMaximumFunctionChainLength();

	/**
	 * Obtains the {@link TeamManagement} to break {@link FunctionState} call
	 * chains.
	 * 
	 * @return {@link TeamManagement} for an active {@link Team}. An active
	 *         {@link Team} contains {@link Thread} instances that will execute the
	 *         {@link Job} with a different {@link Thread} stack.
	 */
	TeamManagement getBreakChainTeamManagement();

	/**
	 * Obtains the {@link ThreadSynchroniserFactory} instances.
	 * 
	 * @return {@link ThreadSynchroniserFactory} instances.
	 */
	ThreadSynchroniserFactory[] getThreadSynchronisers();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Office}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link Office}.
	 */
	EscalationProcedure getOfficeEscalationProcedure();

	/**
	 * Obtains the catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 * 
	 * @return Catch all {@link EscalationFlow} for the {@link OfficeFloor}.
	 */
	EscalationFlow getOfficeFloorEscalation();

}