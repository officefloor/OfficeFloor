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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCallbackFactory;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionState} to spawn a {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateJobNode implements FunctionState {

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link FlowMetaData} for the {@link ThreadState}.
	 */
	private final FlowMetaData<?> flowMetaData;

	/**
	 * Parameter for the initial {@link FunctionState} of the {@link Flow}.
	 */
	private final Object parameter;

	/**
	 * {@link FlowCallbackFactory}.
	 */
	private final FlowCallbackFactory callbackFactory;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop jobNodeDelegator;

	/**
	 * {@link FunctionState} to continue once the {@link ThreadState} has been
	 * spawned.
	 */
	private final FunctionState continueJobNode;

	/**
	 * Instantiate.
	 * 
	 * @param processState
	 *            {@link ProcessState}.
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the initial {@link FunctionState} of the {@link Flow}.
	 * @param callbackFactory
	 *            Optional {@link FlowCallbackFactory}.
	 * @param jobNodeDelegator
	 *            {@link FunctionLoop}.
	 * @param continueJobNode
	 *            {@link FunctionState} to continue once the {@link ThreadState} has
	 *            been spawned.
	 */
	public SpawnThreadStateJobNode(ProcessState processState, FlowMetaData<?> flowMetaData, Object parameter,
			FlowCallbackFactory callbackFactory, FunctionLoop jobNodeDelegator, FunctionState continueJobNode) {
		this.processState = processState;
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
		this.callbackFactory = callbackFactory;
		this.jobNodeDelegator = jobNodeDelegator;
		this.continueJobNode = continueJobNode;
	}

	/*
	 * ====================== JobNode ============================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.processState.getMainThreadState();
	}

	@Override
	public boolean isRequireThreadStateSafety() {
		return true; // must synchronise on thread state to spawn thread state
	}

	@Override
	public FunctionState execute() {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = this.flowMetaData.getInitialTaskMetaData();

		// Create thread to execute asynchronously
		AssetManager flowAssetManager = this.flowMetaData.getFlowManager();
		ThreadState spawnedThreadState = this.processState.createThread(flowAssetManager, this.callbackFactory);

		// Create job node for execution
		Flow flow = spawnedThreadState.createFlow();
		FunctionState jobNode = flow.createManagedJobNode(initTaskMetaData, null, this.parameter,
				GovernanceDeactivationStrategy.ENFORCE);

		// Delegate the new thead to be executed
		this.jobNodeDelegator.delegateFunction(jobNode);

		// Continue on with current thread tasks
		return this.continueJobNode;
	}

}