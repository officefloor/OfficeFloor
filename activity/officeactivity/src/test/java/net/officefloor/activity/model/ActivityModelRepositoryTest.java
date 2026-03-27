/*-
 * #%L
 * Activity
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

package net.officefloor.activity.model;

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link ActivityModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link ActivityModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = FileSystemConfigurationContext
				.createWritableConfigurationItem(this.findFile(this.getClass(), "Activity.activity.xml"));
	}

	/**
	 * Ensure retrieve the {@link ActivityModel}.
	 */
	public void testRetrieveActivity() throws Exception {

		// Load the Activity
		ModelRepository repository = new ModelRepositoryImpl();
		ActivityModel activity = new ActivityModel();
		repository.retrieve(activity, this.configurationItem);

		// ----------------------------------------
		// Validate the Inputs
		// ----------------------------------------
		List<ActivityInputModel> inputs = activity.getActivityInputs();
		assertList(new String[] { "getActivityInputName", "getArgumentType", "getX", "getY" }, inputs,
				new ActivityInputModel("INPUT_1", String.class.getName(), 100, 101),
				new ActivityInputModel("INPUT_2", null, 102, 103), new ActivityInputModel("INPUT_3", null, 104, 105),
				new ActivityInputModel("INPUT_4", null, 106, 107));
		assertProperties(new ActivityInputToActivitySectionInputModel("SECTION_A", "INPUT_A"),
				inputs.get(0).getActivitySectionInput(), "getSectionName", "getInputName");
		assertProperties(new ActivityInputToActivityProcedureModel("PROCEDURE_A"), inputs.get(1).getActivityProcedure(),
				"getProcedureName");
		assertProperties(new ActivityInputToActivityOutputModel("OUTPUT_2"), inputs.get(2).getActivityOutput(),
				"getOutputName");

		// ----------------------------------------
		// Validate the procedures
		// ----------------------------------------
		assertList(
				new String[] { "getActivityProcedureName", "getResource", "getSourceName", "getProcedureName", "getX",
						"getY" },
				activity.getActivityProcedures(),
				new ActivityProcedureModel("PROCEDURE_A", "net.example.ExampleProcedure", "Class", "procedure", 100,
						101),
				new ActivityProcedureModel("PROCEDURE_B", "net.example.KotlinProcedure", "Kotlin", "method", 102, 103),
				new ActivityProcedureModel("PROCEDURE_C", "net.example.ScalaProcedure", "Scala", "func", 104, 105),
				new ActivityProcedureModel("PROCEDURE_D", "net.example.JavaScriptProcedure", "JavaScript", "function",
						106, 107));
		List<ActivityProcedureModel> procedures = activity.getActivityProcedures();
		ActivityProcedureModel procedure = procedures.get(0);
		assertList(new String[] { "getName", "getValue" }, procedure.getProperties(),
				new PropertyModel("name.ONE", "value.ONE"), new PropertyModel("name.TWO", "value.TWO"));

		// Verify next
		assertProperties(new ActivityProcedureNextModel(Byte.class.getName()), procedure.getNext(), "getArgumentType");
		assertProperties(new ActivityProcedureNextModel(null), procedures.get(1).getNext(), "getArgumentType");
		ActivityProcedureNextModel procedureNextSectionInput = procedures.get(0).getNext();
		assertProperties(new ActivityProcedureNextToActivitySectionInputModel("SECTION_A", "INPUT_A"),
				procedureNextSectionInput.getActivitySectionInput(), "getSectionName", "getInputName");
		ActivityProcedureNextModel procedureNextProcedure = procedures.get(1).getNext();
		assertProperties(new ActivityProcedureNextToActivityProcedureModel("PROCEDURE_B"),
				procedureNextProcedure.getActivityProcedure(), "getProcedureName");
		ActivityProcedureNextModel procedureNextOutput = procedures.get(2).getNext();
		assertProperties(new ActivityProcedureNextToActivityOutputModel("OUTPUT_2"),
				procedureNextOutput.getActivityOutput(), "getOutputName");

		// Verify outputs
		assertList(new String[] { "getActivityProcedureOutputName", "getArgumentType" }, procedure.getOutputs(),
				new ActivityProcedureOutputModel("OUTPUT_A", String.class.getName()),
				new ActivityProcedureOutputModel("OUTPUT_B", null), new ActivityProcedureOutputModel("OUTPUT_C", null),
				new ActivityProcedureOutputModel("OUTPUT_D", null));
		ActivityProcedureOutputModel procedureOutputSectionInput = procedure.getOutputs().get(0);
		assertProperties(new ActivityProcedureOutputToActivitySectionInputModel("SECTION_B", "INPUT_0"),
				procedureOutputSectionInput.getActivitySectionInput(), "getSectionName", "getInputName");
		ActivityProcedureOutputModel procedureOutputProcedure = procedure.getOutputs().get(1);
		assertProperties(new ActivityProcedureOutputToActivityProcedureModel("PROCEDURE_B"),
				procedureOutputProcedure.getActivityProcedure(), "getProcedureName");
		ActivityProcedureOutputModel procedureOutputOutput = procedure.getOutputs().get(2);
		assertProperties(new ActivityProcedureOutputToActivityOutputModel("OUTPUT_2"),
				procedureOutputOutput.getActivityOutput(), "getOutputName");

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(
				new String[] { "getActivitySectionName", "getSectionSourceClassName", "getSectionLocation", "getX",
						"getY" },
				activity.getActivitySections(),
				new ActivitySectionModel("SECTION_A", "SECTION", "SECTION_LOCATION", 200, 201),
				new ActivitySectionModel("SECTION_B", "net.example.ExampleSectionSource", "EXAMPLE_LOCATION", 202,
						203));
		ActivitySectionModel section = activity.getActivitySections().get(0);
		assertList(new String[] { "getName", "getValue" }, section.getProperties(),
				new PropertyModel("name.one", "value.one"), new PropertyModel("name.two", "value.two"));
		assertList(new String[] { "getActivitySectionInputName", "getParameterType" }, section.getInputs(),
				new ActivitySectionInputModel("INPUT_A", "java.lang.Integer"),
				new ActivitySectionInputModel("INPUT_B", null));
		assertList(new String[] { "getActivitySectionOutputName", "getArgumentType" }, section.getOutputs(),
				new ActivitySectionOutputModel("OUTPUT_A", "java.lang.String"),
				new ActivitySectionOutputModel("OUTPUT_B", null), new ActivitySectionOutputModel("OUTPUT_C", null),
				new ActivitySectionOutputModel("OUTPUT_D", null));
		ActivitySectionOutputModel sectionOutputSectionInput = section.getOutputs().get(0);
		assertProperties(new ActivitySectionOutputToActivitySectionInputModel("SECTION_B", "INPUT_0"),
				sectionOutputSectionInput.getActivitySectionInput(), "getSectionName", "getInputName");
		ActivitySectionOutputModel sectionOutputProcedure = section.getOutputs().get(1);
		assertProperties(new ActivitySectionOutputToActivityProcedureModel("PROCEDURE_B"),
				sectionOutputProcedure.getActivityProcedure(), "getProcedureName");
		ActivitySectionOutputModel sectionOutputOutput = section.getOutputs().get(2);
		assertProperties(new ActivitySectionOutputToActivityOutputModel("OUTPUT_2"),
				sectionOutputOutput.getActivityOutput(), "getOutputName");

		// ----------------------------------------
		// Validate the exceptions
		// ----------------------------------------
		assertList(new String[] { "getClassName", "getX", "getY" }, activity.getActivityExceptions(),
				new ActivityExceptionModel("java.lang.Exception", 300, 301),
				new ActivityExceptionModel("java.io.IOException", 302, 303),
				new ActivityExceptionModel("java.lang.NullPointerException", 304, 305),
				new ActivityExceptionModel("java.lang.Throwable", 306, 307));
		ActivityExceptionModel exceptionSectionInput = activity.getActivityExceptions().get(0);
		assertProperties(new ActivityExceptionToActivitySectionInputModel("SECTION_A", "INPUT_A"),
				exceptionSectionInput.getActivitySectionInput(), "getSectionName", "getInputName");
		ActivityExceptionModel exceptionProcedure = activity.getActivityExceptions().get(1);
		assertProperties(new ActivityExceptionToActivityProcedureModel("PROCEDURE_A"),
				exceptionProcedure.getActivityProcedure(), "getProcedureName");
		ActivityExceptionModel exceptionOutput = activity.getActivityExceptions().get(2);
		assertProperties(new ActivityExceptionToActivityOutputModel("OUTPUT_2"), exceptionOutput.getActivityOutput(),
				"getOutputName");

		// ----------------------------------------
		// Validate the outputs
		// ----------------------------------------
		assertList(new String[] { "getActivityOutputName", "getParameterType", "getX", "getY" },
				activity.getActivityOutputs(), new ActivityOutputModel("OUTPUT_1", String.class.getName(), 400, 401),
				new ActivityOutputModel("OUTPUT_2", null, 402, 403));
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link ActivityModel}.
	 */
	public void testRoundTripStoreRetrieveActivity() throws Exception {

		// Load the Activity
		ModelRepository repository = new ModelRepositoryImpl();
		ActivityModel activity = new ActivityModel();
		repository.retrieve(activity, this.configurationItem);

		// Store the Activity
		WritableConfigurationItem contents = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(activity, contents);

		// Reload the Activity
		ActivityModel reloadedActivity = new ActivityModel();
		repository.retrieve(reloadedActivity, contents);

		// Validate round trip
		assertGraph(activity, reloadedActivity, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
