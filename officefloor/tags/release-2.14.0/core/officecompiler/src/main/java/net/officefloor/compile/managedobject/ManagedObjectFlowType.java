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
package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a {@link JobSequence} instigated by the
 * {@link ManagedObjectSource} or one of its {@link Task} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link JobSequence}.
	 * 
	 * @return Name of the {@link JobSequence}.
	 */
	String getFlowName();

	/**
	 * Obtains the name of the {@link Work} instigating the {@link JobSequence}.
	 * Should the {@link JobSequence} be instigated by the
	 * {@link ManagedObjectSource} directly (rather than a {@link Task} it
	 * added) this will return <code>null</code>.
	 * 
	 * @return {@link Work} name instigating {@link JobSequence} or
	 *         <code>null</code> if instigated directly by
	 *         {@link ManagedObjectSource}.
	 * 
	 * @see #getTaskName()
	 */
	String getWorkName();

	/**
	 * <p>
	 * Obtains the name of the {@link Task} instigating the {@link JobSequence}.
	 * Should the {@link JobSequence} be instigated by the
	 * {@link ManagedObjectSource} directly (rather than a {@link Task} it
	 * added) this will return <code>null</code>.
	 * <p>
	 * For clarity, this is not the name of the {@link Task} to be invoked by
	 * the {@link JobSequence} but rather the {@link Task} triggering the
	 * {@link JobSequence}. In other words, it is a {@link JobSequence}
	 * invocation that requires to be defined for the
	 * {@link ManagedObjectSource} as it is triggered from a {@link Task} added
	 * by the {@link ManagedObjectSource} and handled by some {@link Office}
	 * {@link Task}.
	 * 
	 * @return {@link Task} name instigating {@link JobSequence} or
	 *         <code>null</code> if instigated directly by
	 *         {@link ManagedObjectSource}.
	 */
	String getTaskName();

	/**
	 * Obtains the key identifying the {@link JobSequence}.
	 * 
	 * @return Key identifying the {@link JobSequence}.
	 */
	F getKey();

	/**
	 * Obtains the index identifying the {@link JobSequence}.
	 * 
	 * @return Index identifying the {@link JobSequence}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed to the {@link JobSequence}.
	 * 
	 * @return Type of argument passed to the {@link JobSequence}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

}