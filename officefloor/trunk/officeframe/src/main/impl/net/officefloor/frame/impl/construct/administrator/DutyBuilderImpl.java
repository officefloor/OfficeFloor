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
package net.officefloor.frame.impl.construct.administrator;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.spi.administration.Duty;

/**
 * {@link DutyBuilder} implementation.
 * 
 * @author Daniel
 */
public class DutyBuilderImpl<A extends Enum<A>, F extends Enum<F>> implements
		DutyBuilder<F>, DutyConfiguration<A> {

	/**
	 * Key identifying the {@link Duty}.
	 */
	private final A dutyKey;

	/**
	 * Registry of {@link Task} instances that may be invoked from the
	 * {@link Duty}.
	 */
	private final Map<Integer, TaskNodeReference> flows = new HashMap<Integer, TaskNodeReference>();

	/**
	 * Initiate.
	 * 
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 */
	public DutyBuilderImpl(A dutyKey) {
		this.dutyKey = dutyKey;
	}

	/*
	 * ================== DutyBuilder =====================================
	 */

	@Override
	public void linkFlow(F key, String workName, String taskName) {
		this.linkFlow(key.ordinal(), workName, taskName);
	}

	@Override
	public void linkFlow(int flowIndex, String workName, String taskName) {
		this.flows.put(new Integer(flowIndex), new TaskNodeReferenceImpl(
				workName, taskName));
	}

	/*
	 * ============== DutyConfiguration ===================================
	 */

	@Override
	public A getDutyKey() {
		return this.dutyKey;
	}

	@Override
	public TaskNodeReference[] getLinkedProcessConfiguration() {

		// Obtain the array size from maximum index
		int arraySize = -1;
		for (Integer key : this.flows.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one up of max index

		// Create the listing of task nodes
		TaskNodeReference[] taskNodes = new TaskNodeReference[arraySize];
		for (Integer key : this.flows.keySet()) {
			TaskNodeReference reference = this.flows.get(key);
			taskNodes[key.intValue()] = reference;
		}

		// Return the listing
		return taskNodes;
	}

}