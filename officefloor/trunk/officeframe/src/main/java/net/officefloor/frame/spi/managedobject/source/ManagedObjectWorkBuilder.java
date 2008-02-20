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
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builds the {@link Work} necessary for a {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectWorkBuilder<W extends Work> {

	/**
	 * Specifies the {@link WorkFactory}.
	 * 
	 * @param factory
	 *            {@link WorkFactory}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void setWorkFactory(WorkFactory<W> factory) throws BuildException;

	/**
	 * Creates the {@link ManagedObjectTaskBuilder} to build a {@link Task} for
	 * this {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @param factory
	 *            {@link TaskFactory}.
	 * @param flowListingEnum
	 *            {@link Enum} providing the listing of {@link Flow} instances.
	 * @return Specific {@link ManagedObjectTaskBuilder}.
	 * @throws BuildException
	 *             If fails to add the {@link Task}.
	 */
	<P extends Object, F extends Enum<F>> ManagedObjectTaskBuilder<F> addTask(
			String taskName, Class<P> parameterType,
			TaskFactory<P, W, None, F> factory, Class<F> flowListingEnum)
			throws BuildException;

	/**
	 * Creates the {@link ManagedObjectTaskBuilder} to build a {@link Task} for
	 * this {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @param factory
	 *            {@link TaskFactory}.
	 * @return Specific {@link TaskBuilder}.
	 * @throws BuildException
	 *             If fails to add the {@link Task}.
	 */
	<P extends Object> ManagedObjectTaskBuilder<Indexed> addTask(
			String taskName, Class<P> parameterType,
			TaskFactory<P, W, None, Indexed> factory) throws BuildException;

}
