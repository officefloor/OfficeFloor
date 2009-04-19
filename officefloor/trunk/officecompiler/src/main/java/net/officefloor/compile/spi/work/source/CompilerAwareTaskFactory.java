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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.TaskModel;

/**
 * {@link TaskFactory} that is compiler aware.
 * 
 * @author Daniel
 */
// TODO remove CompilerAwareTaskFactory
@Deprecated
public interface CompilerAwareTaskFactory<W extends Work, M extends Enum<M>, F extends Enum<F>>
		extends TaskFactory<W, M, F> {

	/**
	 * Initialises the {@link TaskFactory}.
	 * 
	 * @param task
	 *            {@link TaskModel} for the {@link TaskFactory}. A
	 *            {@link TaskModel} is a particular instantiation of a
	 *            {@link Task}.
	 * @throws Exception
	 *             If fails to initialise the {@link TaskFactory}.
	 */
	void initialiseTaskFactory(TaskModel task) throws Exception;

}