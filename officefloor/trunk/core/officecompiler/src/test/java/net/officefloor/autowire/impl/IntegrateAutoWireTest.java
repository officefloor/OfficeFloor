/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireManagementMBean;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Integration test of auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireTest extends OfficeFrameTestCase {

	/**
	 * Dependency object.
	 */
	private static volatile Connection dependencyObject = null;

	/**
	 * {@link Thread} instances that executed the {@link Task} instances.
	 */
	private static final List<Thread> threadForTask = new ArrayList<Thread>(2);

	/**
	 * Registers the {@link Thread} for the {@link Task}.
	 */
	private static void registerTaskThread() {
		synchronized (threadForTask) {
			threadForTask.add(Thread.currentThread());
		}
	}

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can open the {@link OfficeFloor}.
	 */
	public void testOpen() throws Exception {

		// Create the office floor
		final Connection connection = this.createMock(Connection.class);
		final Value value = new Value();
		AutoWireOfficeFloorSource source = this.createSource(value, connection);

		// Open the OfficeFloor
		this.officeFloor = source.openOfficeFloor();

		// Run the task
		this.officeFloor.invokeTask("one.WORK", "doInput", value);

		// Close the OfficeFloor
		this.officeFloor.closeOfficeFloor();

		// Should now not be able to invoke task
		try {
			this.officeFloor.invokeTask("one.WORK", "doInput", value);
			fail("Should not be able to invoke task after closing OfficeFloor");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Must open the Office Floor before obtaining Offices",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can integrate on running a {@link Task}.
	 */
	public void testIntegrationByTask() throws Exception {

		// Ensure in valid state for running test
		synchronized (threadForTask) {
			dependencyObject = null;
			threadForTask.clear();
		}

		// Create the office floor
		final Connection connection = this.createMock(Connection.class);
		final Value value = new Value();
		AutoWireOfficeFloorSource source = this.createSource(value, connection);
		this.officeFloor = source.openOfficeFloor();

		// Invoke the task
		this.officeFloor.invokeTask("one.WORK", "doInput", value);

		// Ensure correct value created
		assertEquals("Incorrect value", "doInput-1", value.value);

		// Validate object and teams
		synchronized (threadForTask) {

			// Ensure appropriate object
			assertEquals("Incorrect dependency", connection, dependencyObject);

			// Ensure appropriate threads executing teams
			assertEquals("Incorrect number of teams", 2, threadForTask.size());
			assertTrue("Should be different threads executing the tasks",
					threadForTask.get(0) != threadForTask.get(1));
		}
	}

	/**
	 * Ensure register/unregister MBean for operational control.
	 */
	public void testMBean() throws Exception {

		// Ensure all auto-wire OfficeFloors are closed
		AutoWireManagement.closeAllOfficeFloors();

		// Create the OfficeFloor
		final Connection connection = this.createMock(Connection.class);
		final Value value = new Value();
		AutoWireOfficeFloorSource source = this.createSource(value, connection);

		// Open the OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Obtain the MBean
		AutoWireManagementMBean[] mbeans = AutoWireManagement
				.getAutoWireManagers();
		assertEquals("Incorrect number of OfficeFloor MBeans", 1, mbeans.length);

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();

		// Ensure MBean unregistered
		AutoWireManagementMBean[] remainingMBeans = AutoWireManagement
				.getAutoWireManagers();
		assertEquals("Should unregister OfficeFloor MBean", 0,
				remainingMBeans.length);
	}

	/**
	 * Creates the configured {@link AutoWireOfficeFloorSource} for testing.
	 * 
	 * @param value
	 *            {@link Value}.
	 * @param connection
	 *            {@link Connection}.
	 * @return {@link AutoWireOfficeFloorSource} for testing.
	 */
	private AutoWireOfficeFloorSource createSource(Value value,
			Connection connection) {

		// Create the office floor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

		// Indicate fail immediately
		source.getOfficeFloorCompiler().setCompilerIssues(
				new FailTestCompilerIssues());

		// Add sections
		AutoWireSection one = source.addSection("one",
				ClassSectionSource.class.getName(),
				MockSectionOne.class.getName());
		AutoWireSection two = source.addSection("two",
				ClassSectionSource.class.getName(),
				MockSectionTwo.class.getName());
		source.link(one, "output", two, "doInput");

		// Add the Value
		source.addObject(value, new AutoWire(Value.class));

		// Add the Connection
		source.addObject(connection, new AutoWire(Connection.class));

		// Provide teams for separate tasks
		source.assignDefaultTeam(OnePersonTeamSource.class.getName());
		source.assignTeam(OnePersonTeamSource.class.getName(), new AutoWire(
				Connection.class));

		// Return the office floor source
		return source;
	}

	/**
	 * Value to be populated.
	 */
	public static class Value {
		public String value = null;
	}

	/**
	 * First mock section.
	 */
	public static class MockSectionOne {
		@NextTask("output")
		public Integer doInput(@Parameter Value value) {
			registerTaskThread();
			value.value = "doInput";
			return new Integer(1);
		}
	}

	/**
	 * Second mock section with object dependency.
	 */
	public static class MockSectionTwo {

		@Dependency
		private Value value;

		public void doInput(@Parameter Integer parameter, Connection connection) {
			synchronized (threadForTask) {
				dependencyObject = connection;
			}
			registerTaskThread();
			this.value.value += "-" + String.valueOf(parameter.intValue());
		}
	}

}