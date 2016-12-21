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
 * Represents a sub-graph of the {@link ManagedFunctionContainer} graph making
 * up the {@link ThreadState}. This enables knowing when to undertake the
 * {@link FlowCallback} on completion of all {@link ManagedFunctionContainer}
 * instances of the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Flow extends LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a new managed {@link ManagedFunctionContainer} contained in this
	 * {@link Flow} for the {@link Task}.
	 * 
	 * @param functionMetaData
	 *            {@link TaskMetaData} for the new
	 *            {@link ManagedFunctionContainer}.
	 * @param parallelNodeOwner
	 *            {@link ManagedFunctionContainer} that is the parallel owner of
	 *            the new {@link ManagedFunctionContainer}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunctionContainer}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return New {@link ManagedFunctionContainer}.
	 */
	ManagedFunctionContainer createManagedFunction(TaskMetaData<?, ?, ?> functionMetaData,
			ManagedFunctionContainer parallelNodeOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Creates a new {@link ManagedFunctionContainer} contained in this
	 * {@link Flow} for the {@link GovernanceActivity}.
	 * 
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelNodeOwner
	 *            {@link ManagedFunctionContainer} that is the parallel owner of
	 *            the new {@link ManagedFunctionContainer}.
	 * @return New {@link ManagedFunctionContainer}.
	 */
	ManagedFunctionContainer createGovernanceNode(GovernanceActivity<?, ?> governanceActivity,
			ManagedFunctionContainer parallelNodeOwner);

	/**
	 * Flags that the input {@link ManagedFunctionContainer} has completed.
	 * 
	 * @param jobNode
	 *            {@link ManagedFunctionContainer} that has completed.
	 * @return Optional {@link FunctionState} to handle completion of the
	 *         {@link ManagedFunctionContainer}.
	 */
	FunctionState managedJobNodeComplete(ManagedFunctionContainer jobNode);

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}