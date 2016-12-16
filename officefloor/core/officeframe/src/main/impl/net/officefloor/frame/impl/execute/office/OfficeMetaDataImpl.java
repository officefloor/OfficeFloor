/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.impl.execute.profile.ProcessProfilerImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.ProcessContextListener;

/**
 * {@link OfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * {@link TeamIdentifier} for invoking the {@link ProcessState}.
	 */
	public static final TeamIdentifier INVOKE_PROCESS_TEAM = new TeamIdentifier() {
	};

	/**
	 * <p>
	 * Convenience method to invoke a {@link Process}.
	 * <p>
	 * This is used by the {@link WorkManagerImpl} and {@link TaskManagerImpl}
	 * for invoking a {@link ProcessState}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param processTicker
	 *            {@link ProcessTicker}.
	 * @return {@link ProcessFuture} for the invoked {@link ProcessState}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be incorrect the {@link Task}.
	 */
	public static ProcessFuture invokeProcess(OfficeMetaData officeMetaData,
			FlowMetaData<?> flowMetaData, Object parameter,
			ProcessTicker processTicker) throws InvalidParameterTypeException {

		// Ensure correct parameter type
		if (parameter != null) {
			Class<?> taskParameterType = flowMetaData.getInitialTaskMetaData()
					.getParameterType();
			if (taskParameterType != null) {
				Class<?> inputParameterType = parameter.getClass();
				if (!taskParameterType.isAssignableFrom(inputParameterType)) {
					throw new InvalidParameterTypeException(
							"Invalid parameter type (input="
									+ inputParameterType.getName()
									+ ", required="
									+ taskParameterType.getName() + ")");
				}
			}
		}

		// Create the job node within a new process
		JobNode jobNode = officeMetaData.createProcess(flowMetaData, parameter,
				null, null, null);

		// Obtain the ProcessState
		ProcessState processState = jobNode.getJobSequence().getThreadState()
				.getProcessState();

		// Indicate process started and register to be notified of completion.
		// Must register before activating job to have trigger on completion.
		if (processTicker != null) {
			processTicker.processStarted();
			processState.registerProcessCompletionListener(processTicker);
		}

		// Assign the job node to the Team
		jobNode.activateJob(INVOKE_PROCESS_TEAM);

		// Indicate when process of work complete
		return processState.getProcessFuture();
	}

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager;

	/**
	 * {@link WorkMetaData} of the {@link Work} that can be done within the
	 * {@link Office}.
	 */
	private final WorkMetaData<?>[] workMetaDatas;

	/**
	 * {@link ProcessMetaData} of the {@link ProcessState} instances created
	 * within this {@link Office}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link ProcessContextListener} instances.
	 */
	private final ProcessContextListener[] processContextListeners;

	/**
	 * {@link OfficeStartupTask} instances.
	 */
	private final OfficeStartupTask[] startupTasks;

	/**
	 * {@link EscalationProcedure} for the {@link Office}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * {@link Profiler}.
	 */
	private final Profiler profiler;

	/**
	 * Required {@link Governance} for the input {@link ManagedObject}
	 * {@link EscalationHandler}.
	 */
	private final boolean[] inputManagedObjectEscalationRequiredGovernance;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeManager
	 *            {@link OfficeManager}.
	 * @param workMetaDatas
	 *            {@link WorkMetaData} of the {@link Work} that can be done
	 *            within the {@link Office}.
	 * @param processMetaData
	 *            {@link ProcessMetaData} of the {@link ProcessState} instances
	 *            created within this {@link Office}.
	 * @param processContextListeners
	 *            {@link ProcessContextListener} instances.
	 * @param startupTasks
	 *            {@link OfficeStartupTask} instances.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure} for the {@link Office}.
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 * @param profiler
	 *            {@link Profiler}.
	 */
	public OfficeMetaDataImpl(String officeName, OfficeManager officeManager,
			WorkMetaData<?>[] workMetaDatas, ProcessMetaData processMetaData,
			ProcessContextListener[] processContextListeners,
			OfficeStartupTask[] startupTasks,
			EscalationProcedure escalationProcedure,
			EscalationFlow officeFloorEscalation, Profiler profiler) {
		this.officeName = officeName;
		this.officeManager = officeManager;
		this.workMetaDatas = workMetaDatas;
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
		this.startupTasks = startupTasks;
		this.escalationProcedure = escalationProcedure;
		this.officeFloorEscalation = officeFloorEscalation;
		this.profiler = profiler;

		// Always no governance for input managed object escalation handling
		GovernanceMetaData<?, ?>[] governanceMetaData = this.processMetaData
				.getThreadMetaData().getGovernanceMetaData();
		this.inputManagedObjectEscalationRequiredGovernance = new boolean[governanceMetaData.length];
		for (int i = 0; i < this.inputManagedObjectEscalationRequiredGovernance.length; i++) {
			this.inputManagedObjectEscalationRequiredGovernance[i] = false;
		}
	}

	/*
	 * ==================== OfficeMetaData ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public OfficeManager getOfficeManager() {
		return this.officeManager;
	}

	@Override
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
	}

	@Override
	public WorkMetaData<?>[] getWorkMetaData() {
		return this.workMetaDatas;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public OfficeStartupTask[] getStartupTasks() {
		return this.startupTasks;
	}

	@Override
	public <W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter, EscalationHandler invocationEscalationHandler,
			TeamManagement escalationResponsibleTeam,
			Team escalationContinueTeam) {
		return this.createProcess(flowMetaData, parameter,
				invocationEscalationHandler, escalationResponsibleTeam,
				escalationContinueTeam, null, null, -1);
	}

	@Override
	public <W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter, EscalationHandler invocationEscalationHandler,
			TeamManagement escalationResponsibleTeam,
			Team escalationContinueTeam, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int processBoundIndexForInputManagedObject) {

		// Create the process profiler (if profiling)
		ProcessProfiler processProfiler = (this.profiler == null ? null
				: new ProcessProfilerImpl(this.profiler, System.nanoTime()));

		// Create the Process State (based on whether have managed object)
		ProcessState processState;
		if (inputManagedObject == null) {
			// Create Process without an Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData,
					this.processContextListeners, this,
					this.officeFloorEscalation, processProfiler,
					invocationEscalationHandler, escalationResponsibleTeam,
					escalationContinueTeam,
					this.inputManagedObjectEscalationRequiredGovernance);

		} else {
			// Create Process with the Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData,
					this.processContextListeners, this,
					this.officeFloorEscalation, processProfiler,
					inputManagedObject, inputManagedObjectMetaData,
					processBoundIndexForInputManagedObject,
					invocationEscalationHandler, escalationResponsibleTeam,
					escalationContinueTeam,
					this.inputManagedObjectEscalationRequiredGovernance);
		}

		// Create the Flow
		AssetManager flowAssetManager = flowMetaData.getFlowManager();
		Flow flow = processState.createThread(flowAssetManager);

		// Obtain the task meta-data
		TaskMetaData<W, ?, ?> taskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create the Job Node for the initial job
		JobNode jobNode = flow.createTaskNode(taskMetaData, null, parameter,
				GovernanceDeactivationStrategy.ENFORCE);

		// Notify of created process context
		Object processIdentifier = processState.getProcessIdentifier();
		for (int i = 0; i < this.processContextListeners.length; i++) {
			this.processContextListeners[i].processCreated(processIdentifier);
		}

		// Return the Job Node
		return jobNode;
	}

}