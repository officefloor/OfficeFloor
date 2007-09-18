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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Links a {@link net.officefloor.frame.internal.structure.WorkContainer} to a
 * {@link net.officefloor.frame.internal.structure.ThreadState}.
 * 
 * @author Daniel
 */
public interface ThreadWorkLink<W extends Work> extends
		LinkedListEntry<ThreadWorkLink<?>> {

	/**
	 * Obtains the {@link WorkContainer} that is linked by this to the
	 * {@link ThreadState}.
	 * 
	 * @return {@link WorkContainer} that is linked by this to the
	 *         {@link ThreadState}.
	 */
	WorkContainer<W> getWorkContainer();

	/**
	 * Obtains the {@link ThreadState} linked by this to the
	 * {@link WorkContainer}.
	 * 
	 * @return {@link ThreadState} linked by this to the {@link WorkContainer}.
	 */
	ThreadState getThreadState();

	/**
	 * Creates a {@link ThreadWorkLink} to the input {@link ThreadState}.
	 * 
	 * @param targetThread
	 *            {@link ThreadState} to link to the {@link WorkContainer} of
	 *            this.
	 * @return {@link ThreadWorkLink} for the input {@link ThreadState}.
	 */
	ThreadWorkLink<W> createThreadWorkLink(ThreadState targetThread);

	/**
	 * Registers a {@link TaskContainer} to use the underlying
	 * {@link WorkContainer} of this {@link ThreadWorkLink}.
	 * 
	 * @param task
	 *            {@link TaskContainer} requiring to use the linked
	 *            {@link WorkContainer}.
	 */
	void registerTask(TaskContainer task);

	/**
	 * Unregisters a {@link TaskContainer} after it has finishd using the
	 * underlying {@link WorkContainer}.
	 * 
	 * @param task
	 *            {@link TaskContainer} that is finished with the
	 *            {@link WorkContainer}.
	 */
	void unregisterTask(TaskContainer task);

}
