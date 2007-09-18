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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Represents a sequence of {@link net.officefloor.frame.api.execute.Task}
 * instances that are completed one after another.
 * 
 * @author Daniel
 */
public interface Flow extends FlowFuture, LinkedListEntry<Flow> {

	/**
	 * Creates a new {@link TaskContainer} bound to this {@link Flow}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link TaskContainer}.
	 * @param parallelNodeOwner
	 *            {@link TaskNode} that is the parallel owner of the new
	 *            {@link TaskContainer}.
	 * @param parameter
	 *            Parameter for the {@link TaskContainer}.
	 * @param currentWorkLink
	 *            {@link ThreadWorkLink} for the {@link WorkContainer} that is
	 *            currently in focus. In other words the {@link ThreadWorkLink}
	 *            of the {@link TaskContainer} creating the new
	 *            {@link TaskContainer}.
	 * @return New configured {@link TaskContainer}.
	 */
	TaskContainer createTaskContainer(TaskMetaData taskMetaData,
			TaskNode parallelNodeOwner, Object parameter,
			ThreadWorkLink currentWorkLink);

	/**
	 * Flags that the input {@link TaskContainer} has completed.
	 * 
	 * @param taskContainer
	 *            {@link TaskContainer} that has completed.
	 */
	void taskContainerComplete(TaskContainer taskContainer);

	/**
	 * Obtains the {@link ThreadState} that this {@link Flow} is bound.
	 * 
	 * @return {@link ThreadState} that this {@link Flow} is bound.
	 */
	ThreadState getThreadState();

}
