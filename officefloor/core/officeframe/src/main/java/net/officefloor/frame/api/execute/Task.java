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
package net.officefloor.frame.api.execute;

import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Task of the {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Task<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Does the task.
	 * 
	 * @param context
	 *            {@link TaskContext} for the {@link Task}.
	 * @return Parameter for the next {@link Task}. This allows stringing
	 *         {@link Task} instances together into a {@link JobSequence}.
	 * @throws Throwable
	 *             Indicating failure of the {@link Task}.
	 */
	Object doTask(TaskContext<W, D, F> context) throws Throwable;

}