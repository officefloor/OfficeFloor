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
package net.officefloor.model.test.changes;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.Model;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Abstract operations {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractChangesTestCase<M extends Model, O> extends
		OfficeFrameTestCase {

	/**
	 * {@link Model} loaded for testing.
	 */
	protected M model;

	/**
	 * Operations.
	 */
	protected O operations;

	/**
	 * Flags if there is a specific setup file per test.
	 */
	private boolean isSpecificSetupFilePerTest;

	/**
	 * Initiate.
	 */
	public AbstractChangesTestCase() {
		this.isSpecificSetupFilePerTest = false;
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest
	 *            Flags if there is a specific setup file per test.
	 */
	public AbstractChangesTestCase(boolean isSpecificSetupFilePerTest) {
		this.isSpecificSetupFilePerTest = isSpecificSetupFilePerTest;
	}

	@Override
	protected void setUp() throws Exception {

		// Retrieve the setup model
		String setupTestName = this.getSetupTestName();
		this.model = this.retrieveModel(setupTestName, null);

		// Create the model operations
		this.operations = this.createModelOperations(this.model);
	}

	/**
	 * Retrieves the {@link Model}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the {@link Model}.
	 * @return {@link Model}.
	 * @throws Exception
	 *             If fails to retrieve the {@link Model}.
	 */
	protected abstract M retrieveModel(ConfigurationItem configurationItem)
			throws Exception;

	/**
	 * Creates the {@link Model} operations.
	 * 
	 * @param model
	 *            {@link Model} to create operations for.
	 * @return {@link Model} operations.
	 */
	protected abstract O createModelOperations(M model);

	/**
	 * Obtains the extension for the file containing the {@link Model}.
	 * 
	 * @return Extension for the file containing the {@link Model}.
	 */
	protected abstract String getModelFileExtension();

	/**
	 * Allows particular tests of a {@link TestCase} to override using the
	 * default setup {@link ConfigurationItem} and use the specific test
	 * {@link ConfigurationItem}.
	 */
	protected void useTestSetupModel() {
		try {
			// Flag to use test specific setup model
			this.isSpecificSetupFilePerTest = true;

			// re-setup the test
			this.setUp();

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			StringWriter msg = new StringWriter();
			ex.printStackTrace(new PrintWriter(msg));
			fail("Failed to useTestSetupModel");
		}
	}

	/**
	 * Obtains the test name for the setup {@link ConfigurationItem}.
	 * 
	 * @return Test name for the setup {@link ConfigurationItem}.
	 */
	private String getSetupTestName() {
		return (this.isSpecificSetupFilePerTest ? this.getName() + "_" : "")
				+ "setup";
	}

	/**
	 * Asserts the {@link Change} is correct.
	 * 
	 * @param change
	 *            {@link Change} to verify.
	 * @param expectedTarget
	 *            Expected target.
	 * @param expectedChangeDescription
	 *            Expected description of the {@link Change}.
	 * @param expectCanApply
	 *            Expected if can apply the {@link Change}. Should it be able to
	 *            be applied, both the {@link Change#apply()} and
	 *            {@link Change#revert()} will be also tested.
	 * @param expectedConflictDescriptions
	 *            Expected descriptions for the {@link Conflict} instances on
	 *            the {@link Change}.
	 */
	protected <T> void assertChange(Change<T> change, T expectedTarget,
			String expectedChangeDescription, boolean expectCanApply,
			String... expectedConflictDescriptions) {

		// Ensure details of change correct
		if (expectedTarget != null) {
			assertEquals("Incorrect target", expectedTarget, change.getTarget());
		}
		assertEquals("Incorrect change description", expectedChangeDescription,
				change.getChangeDescription());
		Conflict[] conflicts = change.getConflicts();
		assertEquals("Incorrect number of conflicts",
				expectedConflictDescriptions.length, conflicts.length);
		for (int i = 0; i < expectedConflictDescriptions.length; i++) {
			assertEquals("Incorrect description for conflict " + i,
					expectedConflictDescriptions[i],
					conflicts[i].getConflictDescription());
		}

		// Validate changes if can apply change
		if (expectCanApply) {
			// Should be no change until change is applied
			this.validateAsSetupModel();

			// Apply the change and validate results
			change.apply();
			this.validateModel();

			// Revert the change and validate reverted back to setup
			change.revert();
			this.validateAsSetupModel();

			// Apply again for 'redo' functionality
			change.apply();
			this.validateModel();

			// Revert change to have model in setup state for any further
			// testing
			change.revert();
		}
	}

	/**
	 * Assets all the {@link Change} instances result in a correct change.
	 * 
	 * @param changes
	 *            {@link Change} instances to verify.
	 */
	protected void assertChanges(Change<?>... changes) {

		// Apply the changes and verify the changes
		for (int i = 0; i < changes.length; i++) {
			changes[i].apply();
		}
		this.validateModel();

		// Revert the changes and verify reverted
		for (int i = (changes.length - 1); i >= 0; i--) {
			changes[i].revert();
		}
		this.validateAsSetupModel();
	}

	/**
	 * Validates the {@link Model} against the default {@link Model} file for
	 * the test.
	 */
	protected void validateModel() {
		this.validateModel(null);
	}

	/**
	 * Validates the {@link Model} against the specific {@link Model} file for
	 * the test.
	 * 
	 * @param specific
	 *            Indicates the specific {@link Model} file for the test.
	 */
	protected void validateModel(String specific) {
		this.validateModel(this.getName(), specific);
	}

	/**
	 * <p>
	 * Validates the {@link Model} against the {@link Model} setup for testing.
	 * <p>
	 * This is useful to test the revert functionality of a {@link Change}.
	 */
	protected void validateAsSetupModel() {
		String setupTestName = this.getSetupTestName();
		this.validateModel(setupTestName, null);
	}

	/**
	 * Validates the {@link Model}.
	 * 
	 * @param testName
	 *            Name of the test.
	 * @param specific
	 *            Specific name for the test. May be <code>null</code> for the
	 *            default {@link Model} for the test.
	 */
	private void validateModel(String testName, String specific) {

		// Obtain the model
		M compareModel = this.retrieveModel(testName, specific);

		try {

			// Assert the models are the same
			this.assertModels(compareModel, this.model);

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			throw fail(ex);
		}
	}

	/**
	 * Asserts the models are the same.
	 * 
	 * @param expected
	 *            Expected model.
	 * @param actual
	 *            Actual model.
	 */
	protected void assertModels(M expected, M actual) throws Exception {

		// Ensure the models are the same
		assertGraph(expected, actual,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

	/**
	 * Retrieves the {@link Model} for the test.
	 * 
	 * @param testName
	 *            Name of the test.
	 * @param specific
	 *            Specific name for the test. May be <code>null</code> for the
	 *            default {@link Model} for the test.
	 * @return {@link Model}.
	 */
	private M retrieveModel(String testName, String specific) {

		// Move the 'Test' to start of test case name
		String testCasePath = this.getClass().getSimpleName();
		testCasePath = this.getClass().getPackage().getName().replace('.', '/')
				+ "/Test"
				+ testCasePath.substring(0,
						(testCasePath.length() - "Test".length()));

		// Construct the path to the model
		String testPath = testCasePath.replace('.', '/') + "/" + testName;
		String modelPath = testPath + (specific == null ? "" : "/" + specific)
				+ this.getModelFileExtension();

		try {
			// Obtain the configuration item to the model
			ConfigurationItem item = new ClassLoaderConfigurationContext(this
					.getClass().getClassLoader())
					.getConfigurationItem(modelPath);
			assertNotNull("Can not find model configuration: " + modelPath,
					item);

			// Return the retrieved model
			return this.retrieveModel(item);

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			StringWriter msg = new StringWriter();
			ex.printStackTrace(new PrintWriter(msg));
			fail("Failed to retrieveModel: " + modelPath + "\n"
					+ msg.toString());
			return null; // fail will throw
		}
	}

}