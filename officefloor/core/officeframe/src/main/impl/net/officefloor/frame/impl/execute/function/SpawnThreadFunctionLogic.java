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

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionLogic} to spawn a {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnThreadFunctionLogic implements FunctionLogic {

	/**
	 * {@link FlowMetaData} for the {@link ThreadState}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter for the initial {@link ManagedFunction} of the {@link Flow}.
	 */
	private final Object parameter;

	/**
	 * {@link FlowCallback}.
	 */
	private final FlowCallback callback;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * Instantiate.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction} of the
	 *            {@link Flow}.
	 * @param callback
	 *            Optional {@link FlowCallback}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public SpawnThreadFunctionLogic(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			FunctionLoop functionLoop) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
		this.callback = callback;
		this.functionLoop = functionLoop;
	}

	/*
	 * ====================== FunctionState ============================
	 */

	@Override
	public boolean isRequireThreadStateSafety() {
		return true; // must synchronise on thread state to spawn thread state
	}

	@Override
	public FunctionState execute(Flow flow) {

		// Obtain the task meta-data for instigating the flow
		ManagedFunctionMetaData<?, ?> initialFunctionMetaData = this.flowMetaData.getInitialFunctionMetaData();

		// Obtain the process state
		ProcessState processState = flow.getThreadState().getProcessState();

		// Create thread to execute asynchronously
		AssetManager flowAssetManager = this.flowMetaData.getFlowManager();
		ThreadState spawnedThreadState = processState.createThread(flowAssetManager, this.callback);

		// Create initial function for execution
		Flow spawnedFlow = spawnedThreadState.createFlow(null);
		FunctionState function = spawnedFlow.createManagedFunction(initialFunctionMetaData, null, this.parameter,
				GovernanceDeactivationStrategy.ENFORCE);

		// Delegate the new thead to be executed
		this.functionLoop.delegateFunction(function);

		// Thread spawned
		return null;
	}

}