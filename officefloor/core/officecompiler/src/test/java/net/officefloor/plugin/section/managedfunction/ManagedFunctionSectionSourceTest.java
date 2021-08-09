/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.section.managedfunction;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;

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
		SectionObject integerObject = expected.addSectionObject(
				MockQualification.class.getName() + "-" + Integer.class.getName(), Integer.class.getName());
		integerObject.setTypeQualifier(MockQualification.class.getName());
		SectionObject connectionObject = expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());
		SectionObject qualifiedConnectionObject = expected.addSectionObject(
				MockQualification.class.getName() + "-" + Connection.class.getName(), Connection.class.getName());
		qualifiedConnectionObject.setTypeQualifier(MockQualification.class.getName());
		SectionObject listObject = expected.addSectionObject(List.class.getName(), List.class.getName());

		// Functions
		SectionFunctionNamespace namespace = expected.addSectionFunctionNamespace("NAMESPACE",
				ClassManagedFunctionSource.class.getName());
		namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());

		SectionFunction functionOne = namespace.addSectionFunction("functionOne", "functionOne");
		FunctionObject functionOneConnection = functionOne
				.getFunctionObject(MockQualification.class.getName() + "-" + Connection.class.getName());
		expected.link(functionOneConnection, qualifiedConnectionObject);

		SectionFunction functionTwo = namespace.addSectionFunction("functionTwo", "functionTwo");
		FunctionObject functionTwoInteger = functionTwo
				.getFunctionObject(MockQualification.class.getName() + "-" + Integer.class.getName());
		expected.link(functionTwoInteger, integerObject);
		FunctionObject functionTwoConnection = functionTwo.getFunctionObject(Connection.class.getName());
		expected.link(functionTwoConnection, connectionObject);
		functionTwo.getFunctionObject(String.class.getName()).flagAsParameter();
		FunctionObject functionTwoList = functionTwo.getFunctionObject(List.class.getName());
		expected.link(functionTwoList, listObject);

		namespace.addSectionFunction("functionThree", "functionThree");

		// Validate the type
		SectionLoaderUtil.validateSection(expected, ManagedFunctionSectionSource.class,
				ClassManagedFunctionSource.class.getName(),
				ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "functionTwo", "3",
				ManagedFunctionSectionSource.PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS, "functionTwo , functionThree",
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure appropriately executes {@link ManagedFunction}.
	 */
	public void testExecute() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final List<String> list = new LinkedList<String>();

		// Open the section
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {

			architect.enableAutoWireObjects();

			Singleton.load(architect, connection);
			Singleton.load(architect, list);
			Singleton.load(architect, "ONE", Integer.valueOf(1));
			Singleton.load(architect, "TWO", Integer.valueOf(2), new AutoWire(MockQualification.class, Integer.class));

			// Create section
			OfficeSection section = architect.addOfficeSection("SECTION", ManagedFunctionSectionSource.class.getName(),
					ClassManagedFunctionSource.class.getName());
			section.addProperty(ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "functionTwo", "3");
			section.addProperty(ManagedFunctionSectionSource.PROPERTY_FUNCTIONS_NEXT_TO_OUTPUTS, "functionTwo");
			section.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());

			// Create handle section
			OfficeSection handle = architect.addOfficeSection("HANDLE", ManagedFunctionSectionSource.class.getName(),
					ClassManagedFunctionSource.class.getName());
			handle.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockFinishFunction.class.getName());

			// Link flows
			architect.link(section.getOfficeSectionOutput("doFlow"), handle.getOfficeSectionInput("function"));
			architect.link(section.getOfficeSectionOutput("functionTwo"), handle.getOfficeSectionInput("function"));
		});

		try {

			// Ensure appropriate state for running
			assertEquals("List should be empty before invoking function", 0, list.size());

			// Invoke the function
			final String PARAMETER = "test";
			officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.functionTwo").invokeProcess(PARAMETER, null);

			// Ensure invoked as parameter should be in list
			assertEquals("Parameter not added to list", 2, list.size());
			assertEquals("Incorrect parameter added to list", PARAMETER + "2", list.get(0));
			assertEquals("Should be flagged as finished in list", "Finished", list.get(1));

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

		public Long functionOne(@MockQualification Connection connection, Flows flows)
				throws IOException, SQLException {
			return Long.valueOf(1);
		}

		public Byte functionTwo(@MockQualification Integer number, Connection connection, String value,
				List<String> returnList) throws SQLException {
			returnList.add(value + number);
			return Byte.valueOf((byte) 1);
		}

		public void functionThree(Flows flows) throws IOException {
		}
	}

	/**
	 * Mock {@link Class} for handling output flows for testing.
	 */
	public static class MockFinishFunction {
		public void function(List<String> returnList) {
			returnList.add("Finished");
		}
	}

}
