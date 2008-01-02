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
package net.officefloor.work;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.FlowItemModel;

/**
 * {@link TaskFactory} that is compiler aware.
 * 
 * @author Daniel
 */
public interface CompilerAwareTaskFactory<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		extends TaskFactory<P, W, M, F> {

	/**
	 * Initialises the {@link TaskFactory}.
	 * 
	 * @param task
	 *            {@link FlowItemModel} for the {@link TaskFactory}. A
	 *            {@link FlowItemModel} is a particular instantiation of a
	 *            {@link Task}.
	 * @throws Exception
	 *             If fails to initialise the {@link TaskFactory}.
	 */
	void initialiseTaskFactory(FlowItemModel task) throws Exception;
}
