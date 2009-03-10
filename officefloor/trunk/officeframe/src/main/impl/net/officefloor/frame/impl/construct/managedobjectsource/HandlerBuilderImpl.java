/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * Implementation of the {@link HandlerBuilder}.
 * 
 * @author Daniel
 */
public class HandlerBuilderImpl<H extends Enum<H>, F extends Enum<F>>
		implements HandlerBuilder<F>, HandlerConfiguration<H, F> {

	/**
	 * Key for the {@link Handler}.
	 */
	protected final H handlerKey;

	/**
	 * {@link Enum} specifying keys of the process flows.
	 */
	protected final Class<F> processListingEnum;

	/**
	 * Registry of {@link Task} instances that may be invoked from the
	 * {@link Handler}.
	 */
	protected final Map<Integer, HandlerFlowConfigurationImpl> processes = new HashMap<Integer, HandlerFlowConfigurationImpl>();

	/**
	 * {@link HandlerFactory}.
	 */
	protected HandlerFactory<F> factory;

	/**
	 * Initiate.
	 * 
	 * @param handlerKey
	 *            Key for the {@link Handler}.
	 * @param processListingEnum
	 *            {@link Enum} specifying the keys to the flows from this
	 *            handler. May be <code>null</code> if no flows.
	 */
	public HandlerBuilderImpl(H handlerKey, Class<F> processListingEnum) {
		this.handlerKey = handlerKey;
		this.processListingEnum = processListingEnum;
	}

	/*
	 * ============= HandlerBuilder =======================================
	 */

	@Override
	public void setHandlerFactory(HandlerFactory<F> factory) {
		this.factory = factory;
	}

	@Override
	public void linkProcess(F key, String workName, String taskName) {
		this.linkProcess(key.ordinal(), key, workName, taskName);
	}

	@Override
	public void linkProcess(int processIndex, String workName, String taskName) {
		this.linkProcess(processIndex, null, workName, taskName);
	}

	/**
	 * Links the process.
	 * 
	 * @param processIndex
	 *            Index of the process.
	 * @param flowKey
	 *            Key of the flow.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 */
	private void linkProcess(int processIndex, F flowKey, String workName,
			String taskName) {

		// Determine the name of the flow
		String flowName = (flowKey != null ? flowKey.toString() : String
				.valueOf(processIndex));

		// Create the task node reference
		TaskNodeReferenceImpl taskNodeReference = (taskName == null ? null
				: new TaskNodeReferenceImpl(workName, taskName));

		// Create the handler flow configuration
		HandlerFlowConfigurationImpl handlerFlow = new HandlerFlowConfigurationImpl(
				flowKey, flowName, taskNodeReference);

		// Register the handler flow
		this.processes.put(new Integer(processIndex), handlerFlow);
	}

	/*
	 * ================== HandlerBuilder ==================================
	 */

	@Override
	public H getHandlerKey() {
		return this.handlerKey;
	}

	@Override
	public HandlerFactory<F> getHandlerFactory() {
		return this.factory;
	}

	@Override
	public HandlerFlowConfiguration<F>[] getLinkedProcessConfiguration() {
		return ConstructUtil.toArray(this.processes,
				new HandlerFlowConfiguration[0]);
	}

	/**
	 * {@link HandlerFlowConfiguration} implementation.
	 */
	protected class HandlerFlowConfigurationImpl implements
			HandlerFlowConfiguration<F> {

		/**
		 * Flow key.
		 */
		private final F flowKey;

		/**
		 * Flow name.
		 */
		private final String flowName;

		/**
		 * {@link TaskNodeReference}.
		 */
		public TaskNodeReference taskNodeReference;

		/**
		 * Initiate with flow key.
		 * 
		 * @param flowKey
		 *            Flow key.
		 * @param flowName
		 *            Name of flow.
		 * @param taskNodeReference
		 *            {@link TaskNodeReference}.
		 */
		public HandlerFlowConfigurationImpl(F flowKey, String flowName,
				TaskNodeReference taskNodeReference) {
			this.flowKey = flowKey;
			this.flowName = flowName;
			this.taskNodeReference = taskNodeReference;
		}

		/*
		 * ================= HandlerFlowConfiguration ===================
		 */

		@Override
		public F getFlowKey() {
			return this.flowKey;
		}

		@Override
		public String getFlowName() {
			return this.flowName;
		}

		@Override
		public TaskNodeReference getTaskNodeReference() {
			return this.taskNodeReference;
		}
	}

}
