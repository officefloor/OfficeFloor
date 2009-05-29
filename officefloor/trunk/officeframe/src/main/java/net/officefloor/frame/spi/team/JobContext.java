/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.spi.team;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context for the execution of a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobContext {

	/**
	 * <p>
	 * Obtains the current time in milliseconds.
	 * <p>
	 * This should return similar to {@link System#currentTimeMillis()} but is
	 * provided to cache time for multiple quick operations that require only
	 * estimates of time - such as check if asynchronous operations by the
	 * {@link ManagedObject} instances have not timed out.
	 * <p>
	 * Note CPU operations should be in the nano-seconds.
	 * 
	 * @return Time measured in milliseconds.
	 */
	long getTime();

	/**
	 * Indicates whether to continue execution. This provides a hint to allow a
	 * {@link Job} to be more responsive in shutting down.
	 * 
	 * @return <code>true</code> if should continue executing or
	 *         <code>false</code> if execution has stopped and the {@link Job}
	 *         should stop.
	 */
	boolean continueExecution();

}
