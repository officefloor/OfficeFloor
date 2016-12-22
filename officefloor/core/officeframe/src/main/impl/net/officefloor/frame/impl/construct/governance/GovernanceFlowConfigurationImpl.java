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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.configuration.GovernanceFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link GovernanceFlowConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFlowConfigurationImpl<F extends Enum<F>> implements
		GovernanceFlowConfiguration<F> {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	private final FlowInstigationStrategyEnum strategy;

	/**
	 * Reference to the initial {@link ManagedFunction} of this {@link Flow}.
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
	 *            Reference to the initial {@link ManagedFunction} of this
	 *            {@link Flow}.
	 * @param index
	 *            Index of this {@link Flow}.
	 * @param key
	 *            Key of the {@link Flow}.
	 */
	public GovernanceFlowConfigurationImpl(String flowName,
			FlowInstigationStrategyEnum strategy,
			TaskNodeReference taskNodeRef, int index, F key) {
		this.flowName = flowName;
		this.strategy = strategy;
		this.taskNodeRef = taskNodeRef;
		this.index = index;
		this.key = key;
	}

	/*
	 * ==================== GovernanceFlowConfiguration =======================
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