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
package net.officefloor.frame.spi.administration;

import java.util.List;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;

/**
 * Context in which the
 * {@link net.officefloor.frame.spi.administration.Administrator} executes.
 * 
 * @author Daniel
 */
public interface DutyContext<I extends Object, F extends Enum<F>> {

	/**
	 * Obtains the particular extension interfaces.
	 * 
	 * @return Extension interfaces for the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances to be administered.
	 */
	List<I> getExtensionInterfaces();

	/**
	 * <p>
	 * Instigates a flow to be run in parallel to the {@link Task} being
	 * administered.
	 * 
	 * @param key
	 *            Key identifying the flow to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the flow to be run.
	 */
	void doFlow(F key, Object parameter);

	/**
	 * <p>
	 * Similar to {@link #doFlow(F, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * flows available.
	 * 
	 * @param flowIndex
	 *            Index identifying the flow to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the flow to be run.
	 */
	void doFlow(int flowIndex, Object parameter);

}
