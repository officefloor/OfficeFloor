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
package net.officefloor.frame.api.execute;

/**
 * Task of the {@link net.officefloor.frame.api.execute.Work}.
 * 
 * @author Daniel
 */
public interface Task<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Does the task.
	 * 
	 * @param context
	 *            Context for the {@link Task}.
	 * @return Parameter for the next {@link Task}. This allows stringing
	 *         {@link Task} instances together into a
	 *         {@link net.officefloor.frame.internal.structure.Flow}.
	 * @throws Exception
	 *             Indicating failure of the {@link Task}. A thrown
	 *             {@link Exception} will also flag this {@link Task} and all
	 *             its next and parallel {@link Task} instances to not execute.
	 */
	Object doTask(TaskContext<P, W, M, F> context) throws Exception;

}
