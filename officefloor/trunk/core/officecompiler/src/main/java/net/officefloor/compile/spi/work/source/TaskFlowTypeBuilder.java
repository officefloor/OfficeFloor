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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of a possible {@link JobSequence} instigated by the
 * {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskFlowTypeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the {@link Enum} for this {@link TaskFlowTypeBuilder}. This is
	 * required to be set if <code>F</code> is not {@link None} or
	 * {@link Indexed}.
	 * 
	 * @param key
	 *            {@link Enum} for this {@link TaskFlowTypeBuilder}.
	 */
	void setKey(F key);

	/**
	 * <p>
	 * Specifies the type of the argument passed by the {@link Task} to the
	 * {@link JobSequence}.
	 * <p>
	 * Should there be no argument, do not call this method.
	 * 
	 * @param parameterType
	 *            Type of argument passed to {@link JobSequence}.
	 */
	void setArgumentType(Class<?> argumentType);

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link JobSequence}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link JobSequence}. If not set the {@link TaskTypeBuilder} will use the
	 * following order to get a display label:
	 * <ol>
	 * <li>{@link Enum} key name</li>
	 * <li>index value</li>
	 * </ol>
	 * 
	 * @param label
	 *            Display label for the {@link JobSequence}.
	 */
	void setLabel(String label);

}