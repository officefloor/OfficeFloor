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
package net.officefloor.frame.impl.execute.officefloor;

import java.util.Timer;
import java.util.TimerTask;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteContextImpl<F extends Enum<F>> implements
		ManagedObjectExecuteContext<F> {

	/**
	 * {@link TeamIdentifier} for invoking a {@link ProcessState}.
	 */
	public static final TeamIdentifier INVOKE_PROCESS_TEAM = new TeamIdentifier() {
	};

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * Index of the {@link ManagedObject} within the {@link ProcessState}.
	 */
	private final int processMoIndex;

	/**
	 * {@link FlowMetaData} in index order for the {@link ManagedObjectSource}.
	 */
	private final FlowMetaData<?>[] processLinks;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link TeamManagement} of the {@link Team} responsible for the
	 * {@link ManagedObjectSource} {@link Escalation} handling.
	 */
	private final TeamManagement escalationResponsibleTeam;

	/**
	 * {@link Team} to let the worker ({@link Thread}) continue on to execute
	 * the {@link ManagedObjectSource} {@link EscalationHandler}.
	 */
	private final Team escalationContinueTeam;

	/**
	 * {@link ProcessTicker}.
	 */
	private final ProcessTicker processTicker;

	/**
	 * {@link Timer}.
	 */
	private final Timer timer;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 * @param processMoIndex
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState}.
	 * @param processLinks
	 *            {@link FlowMetaData} in index order for the
	 *            {@link ManagedObjectSource}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData} to create {@link ProcessState}
	 *            instances.
	 * @param escalationResponsibleTeam
	 *            {@link TeamManagement} of the {@link Team} responsible for the
	 *            {@link ManagedObjectSource} {@link Escalation} handling.
	 * @param escalationContinueTeam
	 *            {@link Team} to let the worker ({@link Thread}) continue on to
	 *            execute the {@link ManagedObjectSource}
	 *            {@link EscalationHandler}.
	 * @param processTicker
	 *            {@link ProcessTicker}.
	 * @param timer
	 *            {@link Timer}.
	 */
	public ManagedObjectExecuteContextImpl(
			ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData<?>[] processLinks, OfficeMetaData officeMetaData,
			TeamManagement escalationResponsibleTeam,
			Team escalationContinueTeam, ProcessTicker processTicker,
			Timer timer) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.officeMetaData = officeMetaData;
		this.escalationResponsibleTeam = escalationResponsibleTeam;
		this.escalationContinueTeam = escalationContinueTeam;
		this.processTicker = processTicker;
		this.timer = timer;
	}

	/*
	 * =============== ManagedObjectExecuteContext =============================
	 */

	@Override
	public ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject, long delay) {
		return this.invokeProcess(key.ordinal(), parameter, managedObject,
				delay, null);
	}

	@Override
	public ProcessFuture invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject, long delay) {
		return this.invokeProcess(processIndex, parameter, managedObject,
				delay, null);
	}

	@Override
	public ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler) {
		return this.invokeProcess(key.ordinal(), parameter, managedObject,
				delay, escalationHandler);
	}

	@Override
	public ProcessFuture invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler) {

		// Obtain the flow meta-data
		if ((processIndex < 0) || (processIndex >= this.processLinks.length)) {
			String validIndexes = (this.processLinks.length == 0 ? " [no processes linked]"
					: " [valid only 0 to " + (this.processLinks.length - 1)
							+ "]");
			throw new IllegalArgumentException("Invalid process index "
					+ processIndex + validIndexes);
		}
		FlowMetaData<?> flowMetaData = this.processLinks[processIndex];

		// Create the job in a new process
		final JobNode jobNode = this.officeMetaData.createProcess(flowMetaData,
				parameter, escalationHandler, this.escalationResponsibleTeam,
				this.escalationContinueTeam, managedObject,
				this.managedObjectMetaData, this.processMoIndex);

		// Obtain the process state
		ProcessState processState = jobNode.getJobSequence().getThreadState()
				.getProcessState();

		// Indicate process started and register to be notified of completion
		if (this.processTicker != null) {
			this.processTicker.processStarted();
			processState.registerProcessCompletionListener(this.processTicker);
		}

		// Activate the Job
		// Must register before activating job to have trigger on completion.
		if (delay > 0) {
			// Delay activation of Job
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					jobNode.activateJob(INVOKE_PROCESS_TEAM);
				}
			}, delay);

		} else {
			// Activate Job immediately
			jobNode.activateJob(INVOKE_PROCESS_TEAM);
		}

		// Return the Process Future
		return processState.getProcessFuture();
	}
}