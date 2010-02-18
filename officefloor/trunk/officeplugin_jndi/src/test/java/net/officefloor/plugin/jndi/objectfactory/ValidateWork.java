/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.objectfactory;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Invoked within {@link OfficeFloor} instance to ensure the {@link OfficeFloor}
 * is correctly instantiated by the {@link OfficeFloorObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateWork {

	/**
	 * Flag indicating if this {@link Task} was invoked.
	 */
	private static volatile boolean isTaskInvoked = false;

	/**
	 * Resets state.
	 */
	public static void reset() {
		isTaskInvoked = false;
	}

	/**
	 * Indicates if the {@link Task} was invoked.
	 * 
	 * @return <code>true</code> if the {@link Task} was invoked.
	 */
	public static boolean isTaskInvoked() {
		return isTaskInvoked;
	}

	/**
	 * Task to be executed.
	 */
	public void task() {
		// Flag the task invoked
		isTaskInvoked = true;
	}

}