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
package net.officefloor.plugin.section.managedfunction;

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
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.Qualifier;

/**
 * Tests the {@link ManagedFunctionSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(ManagedFunctionSectionSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Inputs
		expected.addSectionInput("functionOne", null);
		expected.addSectionInput("functionTwo", String.class.getName());
		expected.addSectionInput("functionThree", null);

		// Outputs
		expected.addSectionOutput("doFlow", Character.class.getName(), false);
		expected.addSectionOutput("functionTwo", Byte.class.getName(), false);
		expected.addSectionOutput("functionThree", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);
		expected.addSectionOutput(SQLException.class.getName(), SQLException.class.getName(), true);

		// Objects
		SectionObject integerObject = expected.addSectionObject(Integer.class.getName(), Integer.class.getName());
		SectionObject qualifiedConnectionObject = expected.addSectionObject(
				MockQualification.class.getName() + "-" + Connection.class.getName(), Connection.class.getName());
		qualifiedConnectionObject.setTypeQualifier(MockQualification.class.getName());
		SectionObject connectionObject = expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());
		SectionObject listObject = expected.addSectionObject(List.class.getName(), List.class.getName());

		// Functions
		SectionFunctionNamespace namespace = expected.addSectionFunctionNamespace("NAMESPACE",
				ClassManagedFunctionSource.class.getName());
		namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());

		SectionFunction functionOne = namespace.addSectionFunction("functionOne", "functionOne");
		FunctionObject functionOneInteger = functionOne.getFunctionObject(Integer.class.getName());
		expected.link(functionOneInteger, integerObject);
		FunctionObject functionOneConnection = functionOne
				.getFunctionObject(MockQualification.class.getName() + "-" + Connection.class.getName());
		expected.link(functionOneConnection, qualifiedConnectionObject);

		SectionFunction functionTwo = namespace.addSectionFunction("functionTwo", "functionTwo");
		FunctionObject functionTwoConnection = functionTwo.getFunctionObject(Connection.class.getName());
		expected.link(functionTwoConnection, connectionObject);
		functionTwo.getFunctionObject(String.class.getName()).flagAsParameter();
		FunctionObject functionTwoList = functionTwo.getFunctionObject(List.class.getName());
		expected.link(functionTwoList, listObject);

		namespace.addSectionFunction("functionThree", "functionThree");

		// Validate the type
		SectionLoaderUtil.validateSection(expected, ManagedFunctionSectionSource.class,
				ClassManagedFunctionSource.class.getName(),
				ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "functionTwo", "2",
				ManagedFunctionSectionSource.PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS, "functionTwo , functionThree",
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure appropriately executes {@link ManagedFunction}.
	 */
	public void testExecute() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final List<String> list = new LinkedList<String>();

		AutoWireOfficeFloorSource autoWire = new AutoWireOfficeFloorSource();
		autoWire.addObject(connection, new AutoWire(Connection.class),
				new AutoWire(MockQualification.class, Connection.class));
		autoWire.addObject(list, new AutoWire(List.class));
		autoWire.addObject(new Integer(1), new AutoWire(Integer.class));

		// Create section
		AutoWireSection section = autoWire.addSection("SECTION", ManagedFunctionSectionSource.class.getName(),
				ClassManagedFunctionSource.class.getName());
		section.addProperty(ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "functionTwo", "2");
		section.addProperty(ManagedFunctionSectionSource.PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS, "functionTwo");
		section.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());

		// Create handle section
		AutoWireSection handle = autoWire.addSection("HANDLE", ManagedFunctionSectionSource.class.getName(),
				ClassManagedFunctionSource.class.getName());
		handle.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockFinishFunction.class.getName());

		// Link flows
		autoWire.link(section, "doFlow", handle, "function");
		autoWire.link(section, "functionTwo", handle, "function");

		// Open the section
		AutoWireOfficeFloor officeFloor = autoWire.openOfficeFloor();
		try {

			// Ensure appropriate state for running
			synchronized (list) {
				assertEquals("List should be empty before invoking function", 0, list.size());
			}

			// Invoke the function
			final String PARAMETER = "test";
			officeFloor.invokeFunction("SECTION.functionTwo", PARAMETER, null);

			// Ensure invoked as parameter should be in list
			synchronized (list) {
				assertEquals("Parameter not added to list", 2, list.size());
				assertEquals("Incorrect parameter added to list", PARAMETER, list.get(0));
				assertEquals("Should be flagged as finished in list", "Finished", list.get(1));
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
	 * Mock {@link Class} for testing.
	 */
	public static class MockClass {

		@FlowInterface
		public static interface Flows {
			void doFlow(Character parameter);
		}

		public Long functionOne(Integer value, @MockQualification Connection connection, Flows flows)
				throws IOException, SQLException {
			return new Long(1);
		}

		public Byte functionTwo(Connection connection, String value, List<String> returnList) throws SQLException {
			synchronized (returnList) {
				returnList.add(value);
			}
			return new Byte((byte) 1);
		}

		public void functionThree(Flows flows) throws IOException {
		}
	}

	/**
	 * Mock {@link Class} for handling output flows for testing.
	 */
	public static class MockFinishFunction {
		public void function(List<String> returnList) {
			synchronized (returnList) {
				returnList.add("Finished");
			}
		}
	}

}