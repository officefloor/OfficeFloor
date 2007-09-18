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
package net.officefloor.frame.api.execute;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context that the {@link net.officefloor.frame.api.execute.Handler} is to work
 * within.
 * 
 * @author Daniel
 */
public interface HandlerContext<F extends Enum<F>> {

	/**
	 * Instigates a process to be run.
	 * 
	 * @param key
	 *            Key identifying the process to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the process to be run.
	 * @param managedObject
	 *            {@link ManagedObject} for the process to be run.
	 */
	void invokeProcess(F key, Object parameter, ManagedObject managedObject);

	/**
	 * Similar to {@link #invokeProcess(F, Object, ManagedObject)} except that
	 * allows dynamic instigation of processes.
	 * 
	 * @param processIndex
	 *            Index of the process to invoke.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the process to be run.
	 * @param managedObject
	 *            {@link ManagedObject} for the process to be run.
	 */
	void invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject);

}
