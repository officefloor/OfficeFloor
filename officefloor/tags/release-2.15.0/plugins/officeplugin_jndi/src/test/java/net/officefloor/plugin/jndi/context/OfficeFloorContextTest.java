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

import javax.naming.Context;
import javax.naming.InitialContext;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link OfficeFloorContext}, specifically with JNDI.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure that able to use JNDI to instantiate an {@link OfficeFloor}
	 * instance by referencing the configuration file directly.
	 */
	public void testInstantiateOfficeFloorDirectly() throws Exception {

		// Create the initial context
		Context context = new InitialContext();

		// Specify name of OfficeFloor
		String name = ValidateWork.getOfficeFloorJndiName(true);

		// Obtain the OfficeFloor
		Object object = context.lookup(name);
		assertNotNull("No object looked up", object);
		assertTrue("Incorrect object type", object instanceof OfficeFloor);
		OfficeFloor officeFloor = (OfficeFloor) object;

		// Invoke the work to ensure correct OfficeFloor
		ValidateWork.reset();
		ValidateWork.invokeWork(officeFloor, null);
		assertTrue("Task should be invoked", ValidateWork.isTaskInvoked());
	}

	/**
	 * Ensure that able to use JNDI to instantiate an {@link OfficeFloor}
	 * instance by referencing the configuration file indirectly from a
	 * properties file.
	 */
	public void testInstantiateOfficeFloorIndirectly() throws Exception {

		// Create the initial context
		Context context = new InitialContext();

		// Specify name of OfficeFloor
		String name = ValidateWork.getOfficeFloorJndiName(false);

		// Obtain the OfficeFloor
		Object object = context.lookup(name);
		assertNotNull("No object looked up", object);
		assertTrue("Incorrect object type", object instanceof OfficeFloor);
		OfficeFloor officeFloor = (OfficeFloor) object;

		// Invoke the work to ensure correct OfficeFloor
		ValidateWork.reset();
		ValidateWork.invokeWork(officeFloor, null);
		assertTrue("Task should be invoked", ValidateWork.isTaskInvoked());
	}

	/**
	 * Ensures the same {@link OfficeFloor} is returned for equivalent names.
	 */
	public void testInstantiateSameOfficeFloor() throws Exception {

		// Create the initial context
		Context context = new InitialContext();

		// Obtain the OfficeFloor directly
		String directName = ValidateWork.getOfficeFloorJndiName(true);
		OfficeFloor directOfficeFloor = (OfficeFloor) context
				.lookup(directName);
		assertNotNull("Did not obtain directly", directOfficeFloor);

		// Obtain the OfficeFloor indirectly
		String indirectName = ValidateWork.getOfficeFloorJndiName(false);
		OfficeFloor indirectOfficeFloor = (OfficeFloor) context
				.lookup(indirectName);
		assertNotNull("Did not obtain indirectly", indirectOfficeFloor);

		// Should be the same OfficeFloor instance
		assertSame("Should be same OfficeFloor instance", directOfficeFloor,
				indirectOfficeFloor);
	}

}