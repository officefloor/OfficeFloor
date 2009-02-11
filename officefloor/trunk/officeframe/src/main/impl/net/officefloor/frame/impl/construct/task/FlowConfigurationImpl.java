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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link FlowConfiguration} implementation.
 * 
 * @author Daniel
 */
public class FlowConfigurationImpl implements FlowConfiguration {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	private final FlowInstigationStrategyEnum strategy;

	/**
	 * Reference to the initial {@link Task} of this {@link Flow}.
	 */
	private final TaskNodeReference taskNodeRef;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link Flow}.
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param taskNodeRef
	 *            Reference to the initial {@link Task} of this {@link Flow}.
	 */
	public FlowConfigurationImpl(String flowName,
			FlowInstigationStrategyEnum strategy, TaskNodeReference taskNodeRef) {
		this.flowName = flowName;
		this.strategy = strategy;
		this.taskNodeRef = taskNodeRef;
	}

	/*
	 * ======================= FlowConfiguration ==============================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public FlowInstigationStrategyEnum getInstigationStrategy() {
		return this.strategy;
	}

	@Override
	public TaskNodeReference getInitialTask() {
		return this.taskNodeRef;
	}

}