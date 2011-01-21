/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.autowire;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.test.issues.FailCompilerIssues;
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
	private static List<Thread> threadForTask = new ArrayList<Thread>(2);

	/**
	 * Registers the {@link Thread} for the {@link Task}.
	 */
	private static void registerTaskThread() {
		synchronized (threadForTask) {
			threadForTask.add(Thread.currentThread());
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
		OfficeFloor officeFloor = source.openOfficeFloor();

		// Run the task
		source.invokeTask("one.WORK", "doInput", value);

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();

		// Should now not be able to invoke task
		try {
			source.invokeTask("one.WORK", "doInput", value);
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

		// Invoke the task (which also triggers open the OfficeFloor)
		source.invokeTask("one.WORK", "doInput", value);

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
				new FailCompilerIssues());

		// Add sections
		AutoWireSection one = source.addSection("one",
				ClassSectionSource.class, MockSectionOne.class.getName());
		AutoWireSection two = source.addSection("two",
				ClassSectionSource.class, MockSectionTwo.class.getName());
		source.link(one, "output", two, "doInput");

		// Add the Value
		source.addObject(value, Value.class);

		// Add the Connection
		source.addObject(connection, Connection.class);

		// Provide teams for separate tasks
		source.assignDefaultTeam(OnePersonTeamSource.class);
		source.assignTeam(OnePersonTeamSource.class, Connection.class);

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