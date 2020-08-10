/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.manage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensures the external management API of {@link OfficeFloor} responds
 * correctly.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class OfficeFloorExternalManagementTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mock = new MockTestSupport();

	/**
	 * {@link Office} name.
	 */
	private String officeName;

	/**
	 * {@link MockWork} instance.
	 */
	private MockWork mockWork;

	/**
	 * Annotation.
	 */
	private final Object annotation = "Annotation";

	/**
	 * {@link OfficeFloor} to test management.
	 */
	private OfficeFloor officeFloor;

	@BeforeEach
	protected void setUp() throws Exception {

		// Obtain the Office name
		this.officeName = this.construct.getOfficeName();

		// Construct the Function
		this.mockWork = new MockWork();
		ReflectiveFunctionBuilder initialTask = this.construct.constructFunction(this.mockWork, "initialTask");
		initialTask.buildParameter();
		initialTask.getBuilder().addAnnotation(this.annotation);
		this.construct.constructFunction(this.mockWork, "anotherTask").buildParameter();

		// Compile OfficeFloor
		this.officeFloor = this.construct.constructOfficeFloor();

		// Ensure open to allow obtaining meta-data
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Ensure that not able to open {@link OfficeFloor} instance twice.
	 */
	@Test
	public void testOpenOfficeFloorOnce() throws Exception {
		try {
			this.officeFloor.openOfficeFloor();
			fail("Should not be able open twice");
		} catch (IllegalStateException ex) {
			assertEquals(ex.getMessage(), "OfficeFloor is already open", "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link Office} listing.
	 */
	@Test
	public void testOfficeListing() throws UnknownOfficeException {

		// Ensure correct office listing
		String[] officeNames = this.officeFloor.getOfficeNames();
		this.assertNames(officeNames, this.officeName);

		// Ensure able to obtain Office
		Office office = this.officeFloor.getOffice(this.officeName);
		assertNotNull(office, "Must have Office");
	}

	/**
	 * Ensure throws exception on unknown {@link Office} requested.
	 */
	@Test
	public void testUnknownOffice() {
		final String unknownOfficeName = "Unknown Office - adding name to ensure different " + this.officeName;
		try {
			this.officeFloor.getOffice(unknownOfficeName);
			fail("Should not be able to obtain unknown Office");
		} catch (UnknownOfficeException ex) {
			assertEquals(unknownOfficeName, ex.getUnknownOfficeName(), "Incorrect office");
			assertEquals("Unknown Office '" + unknownOfficeName + "'", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} listing.
	 */
	@Test
	public void testFunctionListing() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Ensure correct function list
		String[] functionNames = office.getFunctionNames();
		this.assertNames(functionNames, "initialTask", "anotherTask");

		// Ensure able to obtain FunctionManager
		FunctionManager function = office.getFunctionManager("anotherTask");
		assertNotNull(function, "Must have FunctionManager");
	}

	/**
	 * Ensures throws exception on unknown {@link FunctionManager}.
	 */
	@Test
	public void testUnknownFunction() throws UnknownOfficeException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		final String unknownFunctionName = "Unknown Function";
		try {
			office.getFunctionManager(unknownFunctionName);
			fail("Should not be able to obtain unknown FunctionManager");
		} catch (UnknownFunctionException ex) {
			assertEquals(unknownFunctionName, ex.getUnknownFunctionName(), "Incorrect function");
			assertEquals("Unknown Function '" + unknownFunctionName + "'", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} annotation.
	 */
	@Test
	public void testFunctionAnnotation() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct annotation
		assertEquals(this.annotation, function.getAnnotations()[0], "Incorrect annotation");
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} parameter type.
	 */
	@Test
	public void testFunctionParameterType() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct parameter type
		assertEquals(Connection.class, function.getParameterType(), "Incorrect parameter type");
	}

	/**
	 * Ensure able to invoke the {@link ManagedFunction}.
	 */
	@Test
	public void testInvokeFunction()
			throws UnknownOfficeException, UnknownFunctionException, InvalidParameterTypeException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Invoke the Function
		Connection connection = this.mock.createMock(Connection.class);
		function.invokeProcess(connection, null);

		// Ensure function invoked
		assertTrue(this.mockWork.isInitialTaskInvoked, "Task should be invoked");
	}

	/**
	 * Ensure indicates if invalid parameter type.
	 */
	@Test
	public void testFailInvokeFunctionAsInvalidParameterType() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure fail on invalid parameter
		try {
			function.invokeProcess("Invalid parameter type", null);
			fail("Should not be successful on invalid parameter type");
		} catch (InvalidParameterTypeException ex) {
			assertEquals("Invalid parameter type (input=" + String.class.getName() + ", required="
					+ Connection.class.getName() + ")", ex.getMessage(), "Incorrect failure");
		}
	}

	/**
	 * Asserts the list of names match.
	 * 
	 * @param actual   Actual names.
	 * @param expected Expected names.
	 */
	private void assertNames(String[] actual, String... expected) {

		// Ensure same length
		assertEquals(expected.length, actual.length, "Incorrect number of names");

		// Sort
		Arrays.sort(expected);
		Arrays.sort(actual);

		// Ensure values the same
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i], "Incorrect name " + i);
		}
	}

	/**
	 * Mock functionality.
	 */
	public class MockWork {

		/**
		 * Indicates if the initial {@link ManagedFunction} has been invoked.
		 */
		public boolean isInitialTaskInvoked = false;

		/**
		 * Initial {@link ManagedFunction}.
		 * 
		 * @param parameter Parameter with distinct type for identifying in testing.
		 */
		public void initialTask(Connection parameter) {
			this.isInitialTaskInvoked = true;
		}

		/**
		 * Another task to possibly execute.
		 * 
		 * @param parameter Parameter with distinct type for identifying in testing.
		 */
		public void anotherTask(Integer parameter) {
		}
	}

}
