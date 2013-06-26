/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * Builds the {@link Work} necessary for a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectWorkBuilder<W extends Work> {

	/**
	 * Creates the {@link ManagedObjectTaskBuilder} to build a {@link Task} for
	 * this {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task}.
	 * @return Specific {@link ManagedObjectTaskBuilder}.
	 */
	<D extends Enum<D>, F extends Enum<F>> ManagedObjectTaskBuilder<D, F> addTask(
			String taskName, TaskFactory<W, D, F> taskFactory);

}