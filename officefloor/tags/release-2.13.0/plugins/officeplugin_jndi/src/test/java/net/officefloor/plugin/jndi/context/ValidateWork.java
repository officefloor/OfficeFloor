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
package net.officefloor.plugin.jndi.context;

import javax.resource.spi.work.Work;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;

/**
 * Invoked within {@link OfficeFloor} instance to ensure the {@link OfficeFloor}
 * is correctly instantiated by the {@link OfficeFloorObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateWork {

	/**
	 * Name of package containing the {@link OfficeFloor} configuration.
	 */
	public static final String PACKAGE_NAME = ValidateWork.class.getPackage()
			.getName();

	/**
	 * Obtains the JNDI name for the {@link OfficeFloor} within the
	 * {@link OfficeFloor} schema.
	 * 
	 * @param isDirect
	 *            <code>true</code> to provide direct JNDI name.
	 * @return JNDI resource name to the {@link OfficeFloor}.
	 */
	public static String getOfficeFloorJndiResourceName(boolean isDirect) {
		return PACKAGE_NAME + "/" + (isDirect ? "direct" : "direct");
	}

	/**
	 * Obtains the {@link OfficeFloor} JNDI name.
	 * 
	 * @param isDirect
	 *            <code>true</code> to provide direct JNDI name.
	 * @return JNDI name to the {@link OfficeFloor}.
	 */
	public static String getOfficeFloorJndiName(boolean isDirect) {
		return "officefloor:" + getOfficeFloorJndiResourceName(isDirect);
	}

	/**
	 * Obtains the {@link WorkManager} for this {@link Work} from the
	 * {@link OfficeFloor}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @return {@link WorkManager} for this {@link Work} from the
	 *         {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to obtain the {@link WorkManager}.
	 */
	public static WorkManager getWorkManager(OfficeFloor officeFloor)
			throws Exception {

		// Obtain the work manager
		Office office = officeFloor.getOffice("OFFICE");
		WorkManager workManager = office.getWorkManager("SECTION.WORK");

		// Return the work manager
		return workManager;
	}

	/**
	 * Invokes this {@link Work} on the {@link OfficeFloor}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @param parameter
	 *            Parameter for the {@link Work}.
	 * @throws Exception
	 *             If fails to invoke the {@link Work}.
	 */
	public static void invokeWork(OfficeFloor officeFloor, Object parameter)
			throws Exception {

		// Obtain the work manager
		WorkManager workManager = getWorkManager(officeFloor);

		// Invoke the work
		workManager.invokeWork(parameter);
	}

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