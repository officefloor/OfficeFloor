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
package net.officefloor.autowire.impl;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;

/**
 * Tests the integration using inheritance of link configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireInheritanceTest extends OfficeFrameTestCase {

	/**
	 * Ensure inherit link configuration.
	 */
	public void testInheritLinkConfiguration() throws Exception {

		// Create application
		AutoWireApplication source = new AutoWireOfficeFloorSource();

		// Configure the object and service section
		MockObject object = new MockObject();
		source.addObject(object);
		AutoWireSection service = source.addSection("SERVICE",
				ClassSectionSource.class.getName(),
				MockServiceClass.class.getName());

		// Configure the parent section
		AutoWireSection parent = source.addSection("PARENT",
				ClassSectionSource.class.getName(),
				MockInheritClass.class.getName());
		source.link(parent, "output", service, "service");

		// Configure the child section (inheriting from parent)
		AutoWireSection child = source.addSection("CHILD",
				ClassSectionSource.class.getName(),
				MockInheritClass.class.getName());
		child.setSuperSection(parent);

		// Open the application
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Ensure parent services
		assertNull("Object should not have value for parent test", object.value);
		officeFloor.invokeTask("PARENT.WORK", "task", null);
		assertEquals("Incorrect parent object value", "SERVICED", object.value);

		// Rest and ensure child inherits link configuration to service
		object.value = null;
		assertNull("Object should not have value for child test", object.value);
		officeFloor.invokeTask("CHILD.WORK", "task", null);
		assertEquals("Incorrect child object value", "SERVICED", object.value);
	}

	/**
	 * Mock inherit class.
	 */
	public static class MockInheritClass {
		@NextTask("output")
		public void task() {
		}
	}

	/**
	 * Mock object.
	 */
	public static class MockObject {
		public String value = null;
	}

	/**
	 * Mock service class.
	 */
	public static class MockServiceClass {
		public void service(MockObject object) {
			object.value = "SERVICED";
		}
	}

}