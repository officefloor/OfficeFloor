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

package net.officefloor.model.test.changes;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.function.Supplier;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.Model;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Abstract operations {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractChangesTestCase<M extends Model, O> extends OfficeFrameTestCase {

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
	 * @param isSpecificSetupFilePerTest Flags if there is a specific setup file per
	 *                                   test.
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
	 * @param configurationItem {@link ConfigurationItem} containing the
	 *                          {@link Model}.
	 * @return {@link Model}.
	 * @throws Exception If fails to retrieve the {@link Model}.
	 */
	protected abstract M retrieveModel(ConfigurationItem configurationItem) throws Exception;

	/**
	 * Stores the {@link Model}.
	 * 
	 * @param model             {@link Model}.
	 * @param configurationItem {@link WritableConfigurationItem} to store the
	 *                          {@link Model}.
	 * @throws Exception If fails to store the {@link Model}.
	 */
	protected abstract void storeModel(M model, WritableConfigurationItem configurationItem) throws Exception;

	/**
	 * Creates the {@link Model} operations.
	 * 
	 * @param model {@link Model} to create operations for.
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
	 * Allows particular tests of a {@link TestCase} to override using the default
	 * setup {@link ConfigurationItem} and use the specific test
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
		return (this.isSpecificSetupFilePerTest ? this.getName() + "_" : "") + "setup";
	}

	/**
	 * Asserts the {@link Change} is correct.
	 * 
	 * @param <T>                          Expected target.
	 * @param change                       {@link Change} to verify.
	 * @param expectedTarget               Expected target.
	 * @param expectedChangeDescription    Expected description of the
	 *                                     {@link Change}.
	 * @param expectCanApply               Expected if can apply the {@link Change}.
	 *                                     Should it be able to be applied, both the
	 *                                     {@link Change#apply()} and
	 *                                     {@link Change#revert()} will be also
	 *                                     tested.
	 * @param expectedConflictDescriptions Expected descriptions for the
	 *                                     {@link Conflict} instances on the
	 *                                     {@link Change}.
	 */
	protected <T> void assertChange(Change<T> change, T expectedTarget, String expectedChangeDescription,
			boolean expectCanApply, String... expectedConflictDescriptions) {

		// Ensure details of change correct
		if (expectedTarget != null) {
			assertEquals("Incorrect target", expectedTarget, change.getTarget());
		}
		assertEquals("Incorrect change description", expectedChangeDescription, change.getChangeDescription());
		Conflict[] conflicts = change.getConflicts();
		StringBuilder conflictMessage = new StringBuilder();
		for (Conflict conflict : conflicts) {
			Throwable cause = conflict.getConflictCause();
			if (cause instanceof AssertionError) {
				throw (AssertionError) cause;
			}
			conflictMessage.append(" - " + conflict.getConflictDescription() + "\n");
		}
		assertEquals("Incorrect number of conflicts\n\n" + conflictMessage, expectedConflictDescriptions.length,
				conflicts.length);
		for (int i = 0; i < expectedConflictDescriptions.length; i++) {
			assertEquals("Incorrect description for conflict " + i, expectedConflictDescriptions[i],
					conflicts[i].getConflictDescription());
		}

		// Validate changes if can apply change
		if (expectCanApply) {
			String state = "initial";
			try {
				// Should be no change until change is applied
				this.validateAsSetupModel();
				state = "start";

				// Apply the change and validate results
				change.apply();
				this.validateModel();
				state = "applied";

				// Revert the change and validate reverted back to setup
				change.revert();
				this.validateAsSetupModel();
				state = "reverted";

				// Apply again for 'redo' functionality
				change.apply();
				this.validateModel();
				state = "reapplied";

				// Revert change to setup state for any further testing
				change.revert();
				state = "reverted";

			} catch (AssertionError ex) {
				// Provide detail of where failed to aid diagnosis
				throw new AssertionFailedError(ex.getMessage() + "\n\n" + this.getName() + " failed at state " + state);
			}
		}
	}

	/**
	 * Assets all the {@link Change} instances result in a correct change.
	 * 
	 * @param changes {@link Change} instances to verify.
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
	 * Validates the {@link Model} against the default {@link Model} file for the
	 * test.
	 */
	protected void validateModel() {
		this.validateModel(null);
	}

	/**
	 * Validates the {@link Model} against the specific {@link Model} file for the
	 * test.
	 * 
	 * @param specific Indicates the specific {@link Model} file for the test.
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
	 * @param testName Name of the test.
	 * @param specific Specific name for the test. May be <code>null</code> for the
	 *                 default {@link Model} for the test.
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
	 * @param expected Expected model.
	 * @param actual   Actual model.
	 * @throws Exception If fails.
	 */
	protected void assertModels(M expected, M actual) throws Exception {

		// Lazy generate the message
		Supplier<String> generateMessage = () -> {
			StringWriter buffer = new StringWriter();
			try (PrintWriter msg = new PrintWriter(buffer)) {

				// Provide details of the model compare
				msg.println("=============== MODEL COMPARE ================");

				// Provide details of expected model
				msg.println("------------------ EXPECTED ------------------");
				WritableConfigurationItem expectedConfig = MemoryConfigurationContext
						.createWritableConfigurationItem("location");
				this.storeModel(expected, expectedConfig);
				Reader expectedReader = expectedConfig.getReader();
				for (int character = expectedReader.read(); character != -1; character = expectedReader.read()) {
					msg.write(character);
				}

				// Provide details of actual model
				msg.println("------------------- ACTUAL -------------------");
				WritableConfigurationItem actualConfig = MemoryConfigurationContext
						.createWritableConfigurationItem("location");
				this.storeModel(actual, actualConfig);
				Reader actualReader = actualConfig.getReader();
				for (int character = actualReader.read(); character != -1; character = actualReader.read()) {
					msg.write(character);
				}

				msg.println("================ END COMPARE =================");

			} catch (Exception ex) {
				buffer.append("Failed to write models: " + ex.getMessage());
			}
			return buffer.toString();
		};

		// Determine if output XML of actual
		if (this.isPrintMessages()) {
			this.printMessage(generateMessage.get());
		}

		// Ensure the models are the same
		try {
			assertGraph(expected, actual, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
		} catch (AssertionFailedError ex) {
			// Propagate with details of models
			throw new AssertionFailedError(ex.getMessage() + "\n\n" + generateMessage.get());
		}
	}

	/**
	 * Retrieves the {@link Model} for the test.
	 * 
	 * @param testName Name of the test.
	 * @param specific Specific name for the test. May be <code>null</code> for the
	 *                 default {@link Model} for the test.
	 * @return {@link Model}.
	 */
	private M retrieveModel(String testName, String specific) {

		// Move the 'Test' to start of test case name
		String testCasePath = this.getClass().getSimpleName();
		testCasePath = this.getClass().getPackage().getName().replace('.', '/') + "/Test"
				+ testCasePath.substring(0, (testCasePath.length() - "Test".length()));

		// Construct the path to the model
		String testPath = testCasePath.replace('.', '/') + "/" + testName;
		String modelPath = testPath + (specific == null ? "" : "/" + specific) + this.getModelFileExtension();

		try {
			// Obtain the configuration item to the model
			ConfigurationItem item = new ClassLoaderConfigurationContext(this.getClass().getClassLoader(), null)
					.getConfigurationItem(modelPath, null);
			assertNotNull("Can not find model configuration: " + modelPath, item);

			// Return the retrieved model
			return this.retrieveModel(item);

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			StringWriter msg = new StringWriter();
			ex.printStackTrace(new PrintWriter(msg));
			fail("Failed to retrieveModel: " + modelPath + "\n" + msg.toString());
			return null; // fail will throw
		}
	}

}
