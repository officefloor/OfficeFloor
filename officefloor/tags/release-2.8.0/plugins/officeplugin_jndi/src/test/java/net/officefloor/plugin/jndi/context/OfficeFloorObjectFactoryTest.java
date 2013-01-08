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

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorObjectFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorObjectFactory} to test.
	 */
	private final ObjectFactory officeFloorOfficeFactory = new OfficeFloorObjectFactory();

	/**
	 * Ensure that able to use JNDI to instantiate an {@link OfficeFloor}
	 * instance by referencing the configuration file directly.
	 */
	public void testInstantiateOfficeFloorDirectly() throws Exception {

		// Specify name of OfficeFloor
		Name name = new CompositeName(ValidateWork
				.getOfficeFloorJndiResourceName(true));

		// Obtain the OfficeFloor
		OfficeFloor officeFloor = (OfficeFloor) this.officeFloorOfficeFactory
				.getObjectInstance(null, name, null, null);
		assertNotNull("No OfficeFloor created", officeFloor);

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

		// Specify name of OfficeFloor
		Name name = new CompositeName(ValidateWork
				.getOfficeFloorJndiResourceName(false));

		// Obtain the OfficeFloor
		OfficeFloor officeFloor = (OfficeFloor) this.officeFloorOfficeFactory
				.getObjectInstance(null, name, null, null);
		assertNotNull("No OfficeFloor created", officeFloor);

		// Invoke the work to ensure correct OfficeFloor
		ValidateWork.reset();
		ValidateWork.invokeWork(officeFloor, null);
		assertTrue("Task should be invoked", ValidateWork.isTaskInvoked());
	}

	/**
	 * Ensures the same {@link OfficeFloor} is returned for equivalent names.
	 */
	public void testInstantiateSameOfficeFloor() throws Exception {

		// Obtain the OfficeFloor directly
		Name directName = new CompositeName(ValidateWork
				.getOfficeFloorJndiResourceName(true));
		OfficeFloor directOfficeFloor = (OfficeFloor) this.officeFloorOfficeFactory
				.getObjectInstance(null, directName, null, null);
		assertNotNull("No OfficeFloor created", directOfficeFloor);

		// Obtain the OfficeFloor indirectly
		Name indirectName = new CompositeName(ValidateWork
				.getOfficeFloorJndiResourceName(false));
		OfficeFloor indirectOfficeFloor = (OfficeFloor) this.officeFloorOfficeFactory
				.getObjectInstance(null, indirectName, null, null);
		assertNotNull("No OfficeFloor created", directOfficeFloor);

		// Should be the same OfficeFloor instance
		assertSame("Should be same OfficeFloor instance", directOfficeFloor,
				indirectOfficeFloor);
	}

}