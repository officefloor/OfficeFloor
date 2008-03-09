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
package net.officefloor.frame.impl.construct;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * Implementation of the {@link net.officefloor.frame.api.build.HandlerBuilder}.
 * 
 * @author Daniel
 */
public class HandlerBuilderImpl<H extends Enum<H>, F extends Enum<F>>
		implements HandlerBuilder<F>, HandlerConfiguration<H, F> {

	/**
	 * Key for the {@link net.officefloor.frame.api.execute.Handler}.
	 */
	protected final H handlerKey;

	/**
	 * {@link Enum} specifying keys of the process flows.
	 */
	protected final Class<F> processListingEnum;

	/**
	 * Registry of {@link net.officefloor.frame.api.execute.Task} instances that
	 * may be invoked from the {@link net.officefloor.frame.api.execute.Handler}.
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
	 *            Key for the {@link net.officefloor.frame.api.execute.Handler}.
	 * @param processListingEnum
	 *            {@link Enum} specifying the keys to the flows from this
	 *            handler. May be <code>null</code> if no flows.
	 */
	public HandlerBuilderImpl(H handlerKey, Class<F> processListingEnum) {
		this.handlerKey = handlerKey;
		this.processListingEnum = processListingEnum;
		if (this.processListingEnum != null) {
			// Load flow place holders
			for (F flowKey : processListingEnum.getEnumConstants()) {
				this.processes.put(new Integer(flowKey.ordinal()),
						new HandlerFlowConfigurationImpl(flowKey, null));
			}
		}
	}

	/*
	 * ====================================================================
	 * HandlerBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerBuilder#setHandlerFactory(net.officefloor.frame.api.build.HandlerFactory)
	 */
	public void setHandlerFactory(HandlerFactory<F> factory) {
		this.factory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerBuilder#linkProcess(F,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkProcess(F key, String workName, String taskName)
			throws BuildException {
		this.linkProcess(key.ordinal(), workName, taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerBuilder#linkProcess(int,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkProcess(int processIndex, String workName, String taskName)
			throws BuildException {

		// Determine how to link
		if (this.processListingEnum != null) {
			// Must already exist
			HandlerFlowConfigurationImpl handlerFlow = this.processes
					.get(new Integer(processIndex));
			if (handlerFlow == null) {
				throw new BuildException("Index " + processIndex
						+ " does not align to key on "
						+ this.processListingEnum.getName());
			}

			// Specify the task node
			handlerFlow.taskNodeReference = (taskName == null ? null
					: new TaskNodeReferenceImpl(workName, taskName));

		} else {
			// Always overwrite
			this.processes.put(new Integer(processIndex),
					new HandlerFlowConfigurationImpl(String
							.valueOf(processIndex), new TaskNodeReferenceImpl(
							workName, taskName)));
		}
	}

	/*
	 * ====================================================================
	 * HandlerBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.HandlerConfiguration#getHandlerKey()
	 */
	public H getHandlerKey() {
		return this.handlerKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.HandlerConfiguration#getHandlerFactory()
	 */
	public HandlerFactory<F> getHandlerFactory() {
		return this.factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.HandlerConfiguration#getLinkedProcessConfiguration()
	 */
	@SuppressWarnings("unchecked")
	public HandlerFlowConfiguration<F>[] getLinkedProcessConfiguration() {
		// Create the listing of handler flows
		HandlerFlowConfiguration<F>[] handlerFlows = new HandlerFlowConfiguration[this.processes
				.size()];
		for (int i = 0; i < handlerFlows.length; i++) {
			handlerFlows[i] = this.processes.get(new Integer(i));
		}

		// Return the listing
		return handlerFlows;
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
		 * @param taskNodeReference
		 *            {@link TaskNodeReference}.
		 */
		public HandlerFlowConfigurationImpl(F flowKey,
				TaskNodeReference taskNodeReference) {
			this.flowKey = flowKey;
			this.flowName = this.flowKey.name();
			this.taskNodeReference = taskNodeReference;
		}

		/**
		 * Initiate with flow name.
		 * 
		 * @param flowName
		 *            Name of flow.
		 * @param taskNodeReference
		 *            {@link TaskNodeReference}.
		 */
		public HandlerFlowConfigurationImpl(String flowName,
				TaskNodeReference taskNodeReference) {
			this.flowKey = null;
			this.flowName = flowName;
			this.taskNodeReference = taskNodeReference;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.internal.configuration.HandlerFlowConfiguration#getFlowKey()
		 */
		@Override
		public F getFlowKey() {
			return this.flowKey;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.internal.configuration.HandlerFlowConfiguration#getFlowName()
		 */
		@Override
		public String getFlowName() {
			return this.flowName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.internal.configuration.HandlerFlowConfiguration#getTaskNodeReference()
		 */
		@Override
		public TaskNodeReference getTaskNodeReference() {
			return this.taskNodeReference;
		}

	}

}
