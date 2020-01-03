package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * {@link ThreadMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadMetaDataImpl implements ThreadMetaData {

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link GovernanceMetaData} instances.
	 */
	private final GovernanceMetaData<?, ?>[] governanceMetaData;

	/**
	 * Maximum {@link FunctionState} chain length.
	 */
	private final int maximumFunctionChainLength;

	/**
	 * Break chain {@link TeamManagement}.
	 */
	private final TeamManagement breakChainTeamManagement;

	/**
	 * {@link ThreadSynchroniserFactory} instances.
	 */
	private final ThreadSynchroniserFactory[] threadSynchronisers;

	/**
	 * {@link Office} {@link EscalationProcedure}.
	 */
	private final EscalationProcedure officeEscalationProcedure;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData      {@link ManagedObjectMetaData} instances.
	 * @param governanceMetaData         {@link GovernanceMetaData} instances.
	 * @param maximumFunctionChainLength Maximum {@link FunctionState} chain length.
	 * @param breakChainTeamManagement   Break chain {@link TeamManagement}.
	 * @param threadSynchronisers        {@link ThreadSynchroniserFactory}
	 *                                   instances.
	 * @param officeEscalationProcedure  {@link Office} {@link EscalationProcedure}.
	 * @param officeFloorEscalation      {@link OfficeFloor} {@link EscalationFlow}.
	 */
	public ThreadMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData,
			GovernanceMetaData<?, ?>[] governanceMetaData, int maximumFunctionChainLength,
			TeamManagement breakChainTeamManagement, ThreadSynchroniserFactory[] threadSynchronisers,
			EscalationProcedure officeEscalationProcedure, EscalationFlow officeFloorEscalation) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.governanceMetaData = governanceMetaData;
		this.maximumFunctionChainLength = maximumFunctionChainLength;
		this.breakChainTeamManagement = breakChainTeamManagement;
		this.threadSynchronisers = threadSynchronisers;
		this.officeEscalationProcedure = officeEscalationProcedure;
		this.officeFloorEscalation = officeFloorEscalation;
	}

	/*
	 * ================== ThreadMetaData =============================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public GovernanceMetaData<?, ?>[] getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public int getMaximumFunctionChainLength() {
		return this.maximumFunctionChainLength;
	}

	@Override
	public TeamManagement getBreakChainTeamManagement() {
		return this.breakChainTeamManagement;
	}

	@Override
	public ThreadSynchroniserFactory[] getThreadSynchronisers() {
		return this.threadSynchronisers;
	}

	@Override
	public EscalationProcedure getOfficeEscalationProcedure() {
		return this.officeEscalationProcedure;
	}

	@Override
	public EscalationFlow getOfficeFloorEscalation() {
		return this.officeFloorEscalation;
	}

}