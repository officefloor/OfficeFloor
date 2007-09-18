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

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
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
	 * Registry of {@link net.officefloor.frame.api.execute.Task} instances that
	 * may be invoked from the {@link net.officefloor.frame.api.execute.Handler}.
	 */
	protected final Map<Integer, TaskNodeReference> processes = new HashMap<Integer, TaskNodeReference>();

	/**
	 * {@link HandlerFactory}.
	 */
	protected HandlerFactory<F> factory;

	/**
	 * Initiate.
	 * 
	 * @param handlerKey
	 *            Key for the {@link net.officefloor.frame.api.execute.Handler}.
	 */
	public HandlerBuilderImpl(H handlerKey) {
		this.handlerKey = handlerKey;
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
	public void linkProcess(F key, String workName, String taskName) {
		this.linkProcess(key.ordinal(), workName, taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerBuilder#linkProcess(int,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkProcess(int processIndex, String workName, String taskName) {
		// Register the process
		this.processes.put(new Integer(processIndex),
				new TaskNodeReferenceImpl(workName, taskName));
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
	public TaskNodeReference[] getLinkedProcessConfiguration() {
		// Create the listing of task nodes
		TaskNodeReference[] taskNodes = new TaskNodeReference[this.processes
				.size()];
		for (int i = 0; i < taskNodes.length; i++) {
			taskNodes[i] = this.processes.get(new Integer(i));
		}

		// Return the listing
		return taskNodes;
	}

}
