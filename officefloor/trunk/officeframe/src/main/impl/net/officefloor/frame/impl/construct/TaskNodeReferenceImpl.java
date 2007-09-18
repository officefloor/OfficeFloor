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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.configuration.TaskNodeReference}.
 * 
 * @author Daniel
 */
class TaskNodeReferenceImpl implements TaskNodeReference {

	/**
	 * Name identifying the {@link Work} containing the
	 * {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final String workName;

	/**
	 * Name of the {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final String taskName;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name identifying the {@link Work} containing the
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param taskName
	 *            Name of the {@link net.officefloor.frame.api.execute.Task}.
	 */
	public TaskNodeReferenceImpl(String workName, String taskName) {
		this.workName = workName;
		this.taskName = taskName;
	}

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of the {@link net.officefloor.frame.api.execute.Task}.
	 */
	public TaskNodeReferenceImpl(String taskName) {
		this.workName = null;
		this.taskName = taskName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskNodeReference#getWorkName()
	 */
	public String getWorkName() {
		return this.workName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskNodeReference#getTaskName()
	 */
	public String getTaskName() {
		return this.taskName;
	}

}
