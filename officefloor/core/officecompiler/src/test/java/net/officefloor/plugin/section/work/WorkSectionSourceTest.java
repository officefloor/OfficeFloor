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
package net.officefloor.plugin.section.work;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.plugin.work.clazz.Qualifier;

/**
 * Tests the {@link WorkSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(WorkSectionSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Inputs
		expected.addSectionInput("taskOne", null);
		expected.addSectionInput("taskTwo", String.class.getName());
		expected.addSectionInput("taskThree", null);

		// Outputs
		expected.addSectionOutput("doFlow", Character.class.getName(), false);
		expected.addSectionOutput("taskTwo", Byte.class.getName(), false);
		expected.addSectionOutput("taskThree", null, false);
		expected.addSectionOutput(IOException.class.getName(),
				IOException.class.getName(), true);
		expected.addSectionOutput(SQLException.class.getName(),
				SQLException.class.getName(), true);

		// Objects
		SectionObject integerObject = expected.addSectionObject(
				Integer.class.getName(), Integer.class.getName());
		SectionObject qualifiedConnectionObject = expected.addSectionObject(
				MockQualification.class.getName() + "-"
						+ Connection.class.getName(),
				Connection.class.getName());
		qualifiedConnectionObject.setTypeQualifier(MockQualification.class
				.getName());
		SectionObject connectionObject = expected.addSectionObject(
				Connection.class.getName(), Connection.class.getName());
		SectionObject listObject = expected.addSectionObject(
				List.class.getName(), List.class.getName());

		// Tasks
		SectionFunctionNamespace work = expected.addSectionWork("WORK",
				ClassWorkSource.class.getName());
		work.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockWork.class.getName());

		SectionFunction taskOne = work.addSectionTask("taskOne", "taskOne");
		FunctionObject taskOneInteger = taskOne.getTaskObject(Integer.class
				.getName());
		expected.link(taskOneInteger, integerObject);
		FunctionObject taskOneConnection = taskOne
				.getTaskObject(MockQualification.class.getName() + "-"
						+ Connection.class.getName());
		expected.link(taskOneConnection, qualifiedConnectionObject);

		SectionFunction taskTwo = work.addSectionTask("taskTwo", "taskTwo");
		FunctionObject taskTwoConnection = taskTwo.getTaskObject(Connection.class
				.getName());
		expected.link(taskTwoConnection, connectionObject);
		taskTwo.getTaskObject(String.class.getName()).flagAsParameter();
		FunctionObject taskTwoList = taskTwo.getTaskObject(List.class.getName());
		expected.link(taskTwoList, listObject);

		work.addSectionTask("taskThree", "taskThree");

		// Validate the type
		SectionLoaderUtil.validateSection(expected, WorkSectionSource.class,
				ClassWorkSource.class.getName(),
				WorkSectionSource.PROPERTY_PARAMETER_PREFIX + "taskTwo", "2",
				WorkSectionSource.PROPERTY_TASKS_NEXT_TO_OUTPUTS,
				"taskTwo , taskThree",
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockWork.class.getName());
	}

	/**
	 * Ensure appropriately executes {@link Work}.
	 */
	public void testExecute() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final List<String> list = new LinkedList<String>();

		AutoWireOfficeFloorSource autoWire = new AutoWireOfficeFloorSource();
		autoWire.addObject(connection, new AutoWire(Connection.class),
				new AutoWire(MockQualification.class, Connection.class));
		autoWire.addObject(list, new AutoWire(List.class));
		autoWire.addObject(new Integer(1), new AutoWire(Integer.class));

		// Create Work section
		AutoWireSection section = autoWire.addSection("SECTION",
				WorkSectionSource.class.getName(),
				ClassWorkSource.class.getName());
		section.addProperty(WorkSectionSource.PROPERTY_PARAMETER_PREFIX
				+ "taskTwo", "2");
		section.addProperty(WorkSectionSource.PROPERTY_TASKS_NEXT_TO_OUTPUTS,
				"taskTwo");
		section.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockWork.class.getName());

		// Create handle section
		AutoWireSection handle = autoWire.addSection("HANDLE",
				WorkSectionSource.class.getName(),
				ClassWorkSource.class.getName());
		handle.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockFinishTask.class.getName());

		// Link flows
		autoWire.link(section, "doFlow", handle, "task");
		autoWire.link(section, "taskTwo", handle, "task");

		// Open the section
		AutoWireOfficeFloor officeFloor = autoWire.openOfficeFloor();
		try {

			// Ensure appropriate state for running
			synchronized (list) {
				assertEquals("List should be empty before invoking task", 0,
						list.size());
			}

			// Invoke the task
			final String PARAMETER = "test";
			officeFloor.invokeTask("SECTION.WORK", "taskTwo", PARAMETER);

			// Ensure invoked as parameter should be in list
			synchronized (list) {
				assertEquals("Parameter not added to list", 2, list.size());
				assertEquals("Incorrect parameter added to list", PARAMETER,
						list.get(0));
				assertEquals("Should be flagged as finished in list",
						"Finished", list.get(1));
			}

		} finally {
			// Ensure close
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link Qualifier}.
	 */
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MockQualification {
	}

	/**
	 * Mock {@link Work} for testing.
	 */
	public static class MockWork {

		@FlowInterface
		public static interface Flows {
			void doFlow(Character parameter);
		}

		public Long taskOne(Integer value,
				@MockQualification Connection connection, Flows flows)
				throws IOException, SQLException {
			return new Long(1);
		}

		public Byte taskTwo(Connection connection, String value,
				List<String> returnList) throws SQLException {
			synchronized (returnList) {
				returnList.add(value);
			}
			return new Byte((byte) 1);
		}

		public void taskThree(Flows flows) throws IOException {
		}
	}

	/**
	 * Mock {@link Work} for handling output flows for testing.
	 */
	public static class MockFinishTask {
		public void task(List<String> returnList) {
			synchronized (returnList) {
				returnList.add("Finished");
			}
		}
	}

}