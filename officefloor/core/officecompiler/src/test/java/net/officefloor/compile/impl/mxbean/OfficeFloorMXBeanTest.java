/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.impl.mxbean;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure register {@link OfficeFloor} as an {@link MXBean} with ability to run
 * {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMXBeanTest extends OfficeFrameTestCase {

	/**
	 * Name value for the {@link OfficeFloor}.
	 */
	static final String MBEAN_NAME = "OfficeFloor";

	/**
	 * Obtains the {@link ObjectName} for the {@link OfficeFloor}.
	 * 
	 * @return {@link ObjectName} for the {@link OfficeFloor}.
	 */
	static ObjectName getObjectName() {
		try {
			return new ObjectName("officefloor:type=" + OfficeFloor.class.getName() + ",name=" + MBEAN_NAME + "*");
		} catch (Exception ex) {
			// This should never be the case
			throw new Error(ex);
		}
	}

	/**
	 * Ensure registers {@link OfficeFloor} as an {@link MXBean}.
	 */
	public void testRegisterOfficeFloorAsMXBean() throws Exception {

		// Compile the OfficeFloor
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		OfficeFloor officeFloor = compiler.compile(null);

		// Ensure have name for OfficeFloor
		ObjectName name = getObjectName();
		assertNotNull("Must have object name", name);

		// Ensure not registered yet as MXBean
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		assertFalse("Should not be registered, on just compiling", server.isRegistered(name));

	}

	/**
	 * Ensure can invoke a {@link ManagedFunction}.
	 */
	public void testInvokeManagedFunction() {
		fail("TODO implement");
	}

	/**
	 * Ensure able to close {@link OfficeFloor}.
	 */
	public void testCloseOfficeFloor() {
		fail("TODO implement");
	}

}