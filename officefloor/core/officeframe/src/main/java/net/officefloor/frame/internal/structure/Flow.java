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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.Task;

/**
 * Represents a sub-graph of the {@link ManagedFunction} graph making up the
 * {@link ThreadState}. This enables knowing when to undertake the
 * {@link FlowCallback} on completion of all {@link ManagedFunction} instances of
 * the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Flow extends LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a new managed {@link ManagedFunction} contained in this
	 * {@link Flow} for the {@link Task}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link ManagedFunction}.
	 * @param parallelNodeOwner
	 *            {@link ManagedFunction} that is the parallel owner of the new
	 *            {@link ManagedFunction}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return New {@link ManagedFunction}.
	 */
	ManagedFunction createManagedJobNode(TaskMetaData<?, ?, ?> taskMetaData, ManagedFunction parallelNodeOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Creates a new {@link ManagedFunction} contained in this {@link Flow} for
	 * the {@link GovernanceActivity}.
	 * 
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelNodeOwner
	 *            {@link ManagedFunction} that is the parallel owner of the new
	 *            {@link ManagedFunction}.
	 * @return New {@link ManagedFunction}.
	 */
	ManagedFunction createGovernanceNode(GovernanceActivity<?, ?> governanceActivity, ManagedFunction parallelNodeOwner);

	/**
	 * Flags that the input {@link ManagedFunction} has completed.
	 * 
	 * @param jobNode
	 *            {@link ManagedFunction} that has completed.
	 * @return Optional {@link FunctionState} to handle completion of the
	 *         {@link ManagedFunction}.
	 */
	FunctionState managedJobNodeComplete(ManagedFunction jobNode);

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}