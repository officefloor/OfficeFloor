/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a {@link Flow} instigated by the
 * {@link ManagedObjectSource} or one of its {@link Task} instances.
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
	 * Obtains the name of the {@link Work} instigating the {@link Flow}. Should
	 * the {@link Flow} be instigated by the {@link ManagedObjectSource}
	 * directly (rather than a {@link Task} it added) this will return
	 * <code>null</code>.
	 * 
	 * @return {@link Work} name or <code>null</code>.
	 */
	String getWorkName();

	/**
	 * Obtains the name of the {@link Task} instigating the {@link Flow}. Should
	 * the {@link Flow} be instigated by the {@link ManagedObjectSource}
	 * directly (rather than a {@link Task} it added) this will return
	 * <code>null</code>.
	 * 
	 * @return {@link Task} name or <code>null</code>.
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