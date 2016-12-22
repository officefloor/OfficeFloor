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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a {@link Flow} instigated by the
 * {@link ManagedObjectSource} or one of its {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Flow}.
	 * 
	 * @return Name of the {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the name of the {@link Work} instigating the {@link Flow}.
	 * Should the {@link Flow} be instigated by the
	 * {@link ManagedObjectSource} directly (rather than a {@link ManagedFunction} it
	 * added) this will return <code>null</code>.
	 * 
	 * @return {@link Work} name instigating {@link Flow} or
	 *         <code>null</code> if instigated directly by
	 *         {@link ManagedObjectSource}.
	 * 
	 * @see #getTaskName()
	 */
	String getWorkName();

	/**
	 * <p>
	 * Obtains the name of the {@link ManagedFunction} instigating the {@link Flow}.
	 * Should the {@link Flow} be instigated by the
	 * {@link ManagedObjectSource} directly (rather than a {@link ManagedFunction} it
	 * added) this will return <code>null</code>.
	 * <p>
	 * For clarity, this is not the name of the {@link ManagedFunction} to be invoked by
	 * the {@link Flow} but rather the {@link ManagedFunction} triggering the
	 * {@link Flow}. In other words, it is a {@link Flow}
	 * invocation that requires to be defined for the
	 * {@link ManagedObjectSource} as it is triggered from a {@link ManagedFunction} added
	 * by the {@link ManagedObjectSource} and handled by some {@link Office}
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunction} name instigating {@link Flow} or
	 *         <code>null</code> if instigated directly by
	 *         {@link ManagedObjectSource}.
	 */
	String getTaskName();

	/**
	 * Obtains the key identifying the {@link Flow}.
	 * 
	 * @return Key identifying the {@link Flow}.
	 */
	F getKey();

	/**
	 * Obtains the index identifying the {@link Flow}.
	 * 
	 * @return Index identifying the {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed to the {@link Flow}.
	 * 
	 * @return Type of argument passed to the {@link Flow}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

}