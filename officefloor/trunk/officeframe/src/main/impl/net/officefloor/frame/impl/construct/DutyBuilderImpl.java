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

import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * Implementation of {@link net.officefloor.frame.api.build.DutyBuilder}.
 * 
 * @author Daniel
 */
public class DutyBuilderImpl<A extends Enum<A>, F extends Enum<F>> implements
		DutyBuilder<F>, DutyConfiguration<A> {

	/**
	 * Key identifying the {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	protected final A dutyKey;

	/**
	 * Registry of {@link net.officefloor.frame.api.execute.Task} instances that
	 * may be invoked from the
	 * {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	protected final Map<Integer, TaskNodeReference> flows = new HashMap<Integer, TaskNodeReference>();

	/**
	 * Initiate.
	 * 
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	public DutyBuilderImpl(A dutyKey) {
		this.dutyKey = dutyKey;
	}

	/*
	 * ====================================================================
	 * DutyBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.DutyBuilder#linkFlow(F,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkFlow(F key, String workName, String taskName) {
		this.linkFlow(key.ordinal(), workName, taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.DutyBuilder#linkFlow(int,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkFlow(int flowIndex, String workName, String taskName) {
		// Register the flow
		this.flows.put(new Integer(flowIndex), new TaskNodeReferenceImpl(
				workName, taskName));
	}

	/*
	 * ====================================================================
	 * DutyConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.DutyConfiguration#getDutyKey()
	 */
	public A getDutyKey() {
		return this.dutyKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.DutyConfiguration#getLinkedProcessConfiguration()
	 */
	public TaskNodeReference[] getLinkedProcessConfiguration() {
		// Create the listing of task nodes
		TaskNodeReference[] taskNodes = new TaskNodeReference[this.flows.size()];
		for (int i = 0; i < taskNodes.length; i++) {
			taskNodes[i] = this.flows.get(new Integer(i));
		}

		// Return the listing
		return taskNodes;
	}

}
