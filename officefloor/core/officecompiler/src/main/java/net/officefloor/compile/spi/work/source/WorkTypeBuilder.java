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
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of the {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkTypeBuilder<W extends Work> {

	/**
	 * Specifies the {@link WorkFactory} to create the {@link Work}.
	 * 
	 * @param workFactory
	 *            {@link WorkFactory}.
	 */
	void setWorkFactory(WorkFactory<W> workFactory);

	/**
	 * Adds a {@link TaskTypeBuilder} to this {@link WorkTypeBuilder}
	 * definition.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task}.
	 * @param objectKeysClass
	 *            {@link Enum} providing the keys of the dependent
	 *            {@link Object} instances required by the
	 *            {@link TaskTypeBuilder}. This may be <code>null</code> if the
	 *            {@link TaskTypeBuilder} requires no dependent {@link Object}
	 *            instances or they are {@link Indexed}.
	 * @param flowKeysClass
	 *            {@link Enum} providing the keys of the {@link JobSequence}
	 *            instigated by the {@link TaskTypeBuilder}. This may be
	 *            <code>null</code> if the {@link TaskTypeBuilder} does not
	 *            instigate {@link JobSequence} instances or they are
	 *            {@link Indexed}.
	 * @return {@link TaskTypeBuilder} to provide <code>type definition</code>
	 *         of the added {@link Task}.
	 */
	<D extends Enum<D>, F extends Enum<F>> TaskTypeBuilder<D, F> addTaskType(
			String taskName, TaskFactory<? super W, D, F> taskFactory,
			Class<D> objectKeysClass, Class<F> flowKeysClass);

}