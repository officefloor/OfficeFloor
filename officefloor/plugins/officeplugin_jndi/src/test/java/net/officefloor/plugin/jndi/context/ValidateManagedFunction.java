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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Invoked within {@link OfficeFloor} instance to ensure the {@link OfficeFloor}
 * is correctly instantiated by the {@link OfficeFloorObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateManagedFunction {

	/**
	 * Name of package containing the {@link OfficeFloor} configuration.
	 */
	public static final String PACKAGE_NAME = ValidateManagedFunction.class.getPackage().getName();

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
	 * Obtains the {@link FunctionManager} for this {@link ManagedFunction} from
	 * the {@link OfficeFloor}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @return {@link FunctionManager} for this {@link ManagedFunction} from the
	 *         {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to obtain the {@link FunctionManager}.
	 */
	public static FunctionManager getFunctionManager(OfficeFloor officeFloor) throws Exception {

		// Obtain the function manager
		Office office = officeFloor.getOffice("OFFICE");
		FunctionManager functionManager = office.getFunctionManager("SECTION.function");

		// Return the function manager
		return functionManager;
	}

	/**
	 * Invokes this {@link ManagedFunction} on the {@link OfficeFloor}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @throws Exception
	 *             If fails to invoke the {@link ManagedFunction}.
	 */
	public static void invokeFunction(OfficeFloor officeFloor, Object parameter) throws Exception {

		// Obtain the function manager
		FunctionManager functionManager = getFunctionManager(officeFloor);

		// Invoke the work
		functionManager.invokeProcess(parameter, null);
	}

	/**
	 * Flag indicating if this {@link ManagedFunction} was invoked.
	 */
	private static volatile boolean isFunctionInvoked = false;

	/**
	 * Resets state.
	 */
	public static void reset() {
		isFunctionInvoked = false;
	}

	/**
	 * Indicates if the {@link ManagedFunction} was invoked.
	 * 
	 * @return <code>true</code> if the {@link ManagedFunction} was invoked.
	 */
	public static boolean isFunctionInvoked() {
		return isFunctionInvoked;
	}

	/**
	 * Function to be executed.
	 */
	public void function() {
		// Flag the function invoked
		isFunctionInvoked = true;
	}

}