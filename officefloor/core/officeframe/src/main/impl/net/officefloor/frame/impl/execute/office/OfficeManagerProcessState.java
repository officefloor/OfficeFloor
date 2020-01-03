package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link OfficeManager} {@link ProcessState}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeManagerProcessState implements ProcessState {

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Main {@link ThreadState}.
	 */
	private final ThreadState mainThreadState;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * Instantiate.
	 * 
	 * @param maximumFunctionChainLength Maximum {@link Promise} chain length.
	 * @param breakChainTeamManagement   Break chain {@link TeamManagement}.
	 * @param functionLoop               {@link FunctionLoop}.
	 */
	public OfficeManagerProcessState(int maximumFunctionChainLength, TeamManagement breakChainTeamManagement,
			FunctionLoop functionLoop) {
		this.functionLoop = functionLoop;

		// Create the meta-data for the process and its main thread state
		this.threadMetaData = new ThreadMetaDataImpl(new ManagedObjectMetaData[0], new GovernanceMetaData[0],
				maximumFunctionChainLength, breakChainTeamManagement, new ThreadSynchroniserFactory[0],
				new EscalationProcedureImpl(), null);

		// Create the main thread state
		// Note: purpose of this to enable synchronising changes to office
		this.mainThreadState = new ThreadStateImpl(this.threadMetaData, (FlowCompletion) null, false, this, null);
	}

	/*
	 * ========================= ProcessState =========================
	 */

	@Override
	public Object getProcessIdentifier() {
		return this;
	}

	@Override
	public ThreadState getMainThreadState() {
		return this.mainThreadState;
	}

	@Override
	public boolean isCancelled() {
		return false; // never cancelled
	}

	@Override
	public ProcessManager getProcessManager() {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should not be process managed");
	}

	@Override
	public FunctionState spawnThreadState(ManagedFunctionMetaData<?, ?> managedFunctionMetaData, Object parameter,
			FlowCompletion completion, boolean isEscalationHandlingThreadState) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should not be be spawning threads");
	}

	@Override
	public FunctionState threadComplete(ThreadState thread, FunctionState threadCompletion) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " should not be be completing threads");
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		throw new IllegalStateException(this.getClass().getSimpleName() + " does not have managed objects");
	}

	@Override
	public ManagedObjectCleanup getManagedObjectCleanup() {
		throw new IllegalStateException(
				this.getClass().getSimpleName() + " should not need clean up as does not have managed objects");
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

}