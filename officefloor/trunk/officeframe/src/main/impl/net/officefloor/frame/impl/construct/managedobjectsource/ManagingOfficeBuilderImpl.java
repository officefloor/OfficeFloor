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

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagingOfficeBuilder} implementation.
 * 
 * @author Daniel
 */
public class ManagingOfficeBuilderImpl<F extends Enum<F>> implements
		ManagingOfficeBuilder<F>, ManagingOfficeConfiguration<F> {

	/**
	 * Name of the {@link Office} managing the {@link ManagedObject}.
	 */
	private final String officeName;

	/**
	 * Name to bind the {@link ManagedObject} within the {@link ProcessState} of
	 * the managing {@link Office}.
	 */
	private String processBoundManagedObjectName = null;

	/**
	 * {@link ManagedObjectFlowConfiguration} instances by their index.
	 */
	private final Map<Integer, ManagedObjectFlowConfiguration<F>> flows = new HashMap<Integer, ManagedObjectFlowConfiguration<F>>();

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} managing the {@link ManagedObject}.
	 */
	public ManagingOfficeBuilderImpl(String officeName) {
		this.officeName = officeName;
	}

	/*
	 * ============== ManagingOfficeBuilder ===============================
	 */

	@Override
	public void setProcessBoundManagedObjectName(
			String processBoundManagedObjectName) {
		this.processBoundManagedObjectName = processBoundManagedObjectName;
	}

	@Override
	public void linkProcess(F key, String workName, String taskName) {
		this.linkProcess(key.ordinal(), key, workName, taskName);
	}

	@Override
	public void linkProcess(int flowIndex, String workName, String taskName) {
		this.linkProcess(flowIndex, null, workName, taskName);
	}

	/**
	 * Links in a {@link Flow}.
	 * 
	 * @param index
	 *            Index for the {@link Flow}.
	 * @param key
	 *            Key identifying the {@link Flow}. May be <code>null</code>.
	 * @param workName
	 *            Name of {@link Work}.
	 * @param taskName
	 *            Name of {@link Task}.
	 */
	private void linkProcess(int index, F key, String workName, String taskName) {

		// Create the managed object flow configuration
		ManagedObjectFlowConfiguration<F> flow = new ManagedObjectFlowConfigurationImpl(
				key, null, new TaskNodeReferenceImpl(workName, taskName, null));

		// Register the flow at its index
		this.flows.put(new Integer(index), flow);
	}

	/*
	 * ============= ManagingOfficeConfiguration ==========================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public String getProcessBoundManagedObjectName() {
		return this.processBoundManagedObjectName;
	}

	@Override
	public ManagingOfficeBuilder<F> getBuilder() {
		return this;
	}

	@Override
	public ManagedObjectFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows,
				new ManagedObjectFlowConfiguration[0]);
	}

	/**
	 * {@link ManagedObjectFlowConfiguration} implementation.
	 */
	private class ManagedObjectFlowConfigurationImpl implements
			ManagedObjectFlowConfiguration<F> {

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
		public ManagedObjectFlowConfigurationImpl(F flowKey, String flowName,
				TaskNodeReference taskNodeReference) {
			this.flowKey = flowKey;
			this.flowName = flowName;
			this.taskNodeReference = taskNodeReference;
		}

		/*
		 * ================= ManagedObjectFlowConfiguration ===================
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