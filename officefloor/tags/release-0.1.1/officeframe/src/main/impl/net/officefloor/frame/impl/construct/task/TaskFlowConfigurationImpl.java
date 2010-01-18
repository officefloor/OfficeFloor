/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.configuration.TaskFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link TaskFlowConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskFlowConfigurationImpl<F extends Enum<F>> implements
		TaskFlowConfiguration<F> {

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
	 * Index of the {@link Flow}.
	 */
	private final int index;

	/**
	 * Key of the {@link Flow}.
	 */
	private final F key;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link Flow}.
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param taskNodeRef
	 *            Reference to the initial {@link Task} of this {@link Flow}.
	 * @param index
	 *            Index of this {@link Flow}.
	 * @param key
	 *            Key of the {@link Flow}.
	 */
	public TaskFlowConfigurationImpl(String flowName,
			FlowInstigationStrategyEnum strategy,
			TaskNodeReference taskNodeRef, int index, F key) {
		this.flowName = flowName;
		this.strategy = strategy;
		this.taskNodeRef = taskNodeRef;
		this.index = index;
		this.key = key;
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

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}