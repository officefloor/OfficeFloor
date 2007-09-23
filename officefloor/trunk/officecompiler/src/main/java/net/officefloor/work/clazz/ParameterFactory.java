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
package net.officefloor.work.clazz;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;

/**
 * Creates the parameter for the
 * {@link net.officefloor.work.clazz.ClassTaskFactory}.
 * 
 * @author Daniel
 */
public interface ParameterFactory<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Creates the parameter from the {@link TaskContext}.
	 * 
	 * @param context
	 *            {@link TaskContext}.
	 * @return Parameter.
	 */
	Object createParameter(TaskContext<P, W, M, F> context);
}
