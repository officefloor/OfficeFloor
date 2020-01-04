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

import java.sql.Connection;
import java.util.Arrays;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures the external management API of {@link OfficeFloor} responds
 * correctly.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExternalManagementTest extends AbstractOfficeConstructTestCase {

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the Office name
		this.officeName = this.getOfficeName();

		// Construct the Function
		this.mockWork = new MockWork();
		ReflectiveFunctionBuilder initialTask = this.constructFunction(this.mockWork, "initialTask");
		initialTask.buildParameter();
		initialTask.getBuilder().addAnnotation(this.annotation);
		this.constructFunction(this.mockWork, "anotherTask").buildParameter();

		// Compile OfficeFloor
		this.officeFloor = this.constructOfficeFloor();

		// Ensure open to allow obtaining meta-data
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Ensure that not able to open {@link OfficeFloor} instance twice.
	 */
	public void testOpenOfficeFloorOnce() throws Exception {
		try {
			this.officeFloor.openOfficeFloor();
			fail("Should not be able open twice");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", ex.getMessage(), "OfficeFloor is already open");
		}
	}

	/**
	 * Ensure able to obtain {@link Office} listing.
	 */
	public void testOfficeListing() throws UnknownOfficeException {

		// Ensure correct office listing
		String[] officeNames = this.officeFloor.getOfficeNames();
		this.assertNames(officeNames, this.officeName);

		// Ensure able to obtain Office
		Office office = this.officeFloor.getOffice(this.officeName);
		assertNotNull("Must have Office", office);
	}

	/**
	 * Ensure throws exception on unknown {@link Office} requested.
	 */
	public void testUnknownOffice() {
		final String unknownOfficeName = "Unknown Office - adding name to ensure different " + this.officeName;
		try {
			this.officeFloor.getOffice(unknownOfficeName);
			fail("Should not be able to obtain unknown Office");
		} catch (UnknownOfficeException ex) {
			assertEquals("Incorrect office", unknownOfficeName, ex.getUnknownOfficeName());
			assertEquals("Incorrect cause", "Unknown Office '" + unknownOfficeName + "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} listing.
	 */
	public void testFunctionListing() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Ensure correct function list
		String[] functionNames = office.getFunctionNames();
		this.assertNames(functionNames, "initialTask", "anotherTask");

		// Ensure able to obtain FunctionManager
		FunctionManager function = office.getFunctionManager("anotherTask");
		assertNotNull("Must have FunctionManager", function);
	}

	/**
	 * Ensures throws exception on unknown {@link FunctionManager}.
	 */
	public void testUnknownFunction() throws UnknownOfficeException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		final String unknownFunctionName = "Unknown Function";
		try {
			office.getFunctionManager(unknownFunctionName);
			fail("Should not be able to obtain unknown FunctionManager");
		} catch (UnknownFunctionException ex) {
			assertEquals("Incorrect function", unknownFunctionName, ex.getUnknownFunctionName());
			assertEquals("Incorrect cause", "Unknown Function '" + unknownFunctionName + "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} differentiator.
	 */
	public void testFunctionAnnotation() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct annotation
		assertEquals("Incorrect annotation", this.annotation, function.getAnnotations()[0]);
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} parameter type.
	 */
	public void testFunctionParameterType() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct parameter type
		assertEquals("Incorrect parameter type", Connection.class, function.getParameterType());
	}

	/**
	 * Ensure able to invoke the {@link ManagedFunction}.
	 */
	public void testInvokeFunction()
			throws UnknownOfficeException, UnknownFunctionException, InvalidParameterTypeException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Invoke the Function
		Connection connection = this.createMock(Connection.class);
		function.invokeProcess(connection, null);

		// Ensure function invoked
		assertTrue("Task should be invoked", this.mockWork.isInitialTaskInvoked);
	}

	/**
	 * Ensure indicates if invalid parameter type.
	 */
	public void testFailInvokeFunctionAsInvalidParameterType() throws UnknownOfficeException, UnknownFunctionException {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure fail on invalid parameter
		try {
			function.invokeProcess("Invalid parameter type", null);
			fail("Should not be successful on invalid parameter type");
		} catch (InvalidParameterTypeException ex) {
			assertEquals("Incorrect failure", "Invalid parameter type (input=" + String.class.getName() + ", required="
					+ Connection.class.getName() + ")", ex.getMessage());
		}
	}

	/**
	 * Asserts the list of names match.
	 * 
	 * @param actual
	 *            Actual names.
	 * @param expected
	 *            Expected names.
	 */
	private void assertNames(String[] actual, String... expected) {

		// Ensure same length
		assertEquals("Incorrect number of names", expected.length, actual.length);

		// Sort
		Arrays.sort(expected);
		Arrays.sort(actual);

		// Ensure values the same
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Incorrect name " + i, expected[i], actual[i]);
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
		 * @param parameter
		 *            Parameter with distinct type for identifying in testing.
		 */
		public void initialTask(Connection parameter) {
			this.isInitialTaskInvoked = true;
		}

		/**
		 * Another task to possibly execute.
		 * 
		 * @param parameter
		 *            Parameter with distinct type for identifying in testing.
		 */
		public void anotherTask(Integer parameter) {
		}
	}

}
