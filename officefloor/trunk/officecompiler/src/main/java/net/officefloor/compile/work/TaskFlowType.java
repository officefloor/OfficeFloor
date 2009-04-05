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
package net.officefloor.compile.work;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.work.clazz.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name for the {@link TaskFlowType}.
	 * 
	 * @return Name for the {@link TaskFlowType}.
	 */
	String getFlowName();

	/**
	 * <p>
	 * Obtains the index for the {@link TaskFlowType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link TaskFlowType}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed by the {@link Task} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code>
	 *         to indicate no argument.
	 */
	Class<?> getArgumentType();

	/**
	 * Obtains the {@link Enum} key for the {@link TaskFlowType}.
	 * 
	 * @return {@link Enum} key for the {@link TaskFlowType}. May be
	 *         <code>null</code> if no {@link Enum} for flows.
	 */
	F getKey();

}