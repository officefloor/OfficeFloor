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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * <p>
 * Creates the {@link net.officefloor.frame.api.execute.Task} to be done.
 * </p>
 * <p>
 * Clients must provide the implementation of this to
 * {@link net.officefloor.frame.api.build.TaskBuilder#setTaskFactory(TaskFactory)}.
 * </p>
 * 
 * @author Daniel
 */
public interface TaskFactory<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Creates the {@link Task}.
	 * 
	 * @param work
	 *            {@link Work} for the {@link Task}.
	 * @return {@link Task} to be done for the {@link Work}.
	 */
	Task<P, W, M, F> createTask(W work);

}
