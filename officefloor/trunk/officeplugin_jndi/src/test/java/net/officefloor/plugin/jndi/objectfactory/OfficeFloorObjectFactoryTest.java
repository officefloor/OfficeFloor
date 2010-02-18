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

import javax.naming.Context;
import javax.naming.InitialContext;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorObjectFactoryTest extends OfficeFrameTestCase {

	/**
	 * Package name for this class.
	 */
	private final String packageName = this.getClass().getPackage().getName();

	/**
	 * Ensure that able to use JNDI to instantiate an {@link OfficeFloor}
	 * instance by referencing the configuration file directly.
	 */
	public void testInstantiateOfficeFloorDirectly() throws Exception {

		// Create the initial context
		Context context = new InitialContext();

		// Specify name of OfficeFloor
		String name = "officefloor:" + this.packageName + "/direct";

		// Obtain the OfficeFloor
		Object object = context.lookup(name);
		assertNotNull("No object looked up", object);
		assertTrue("Incorrect object type", object instanceof OfficeFloor);
		OfficeFloor officeFloor = (OfficeFloor) object;

		// Invoke the work to ensure correct OfficeFloor
		ValidateWork.reset();
		Office office = officeFloor.getOffice("OFFICE");
		WorkManager workManager = office.getWorkManager("SECTION.WORK");
		workManager.invokeWork(null);
		assertTrue("Task should be invoked", ValidateWork.isTaskInvoked());
	}

}