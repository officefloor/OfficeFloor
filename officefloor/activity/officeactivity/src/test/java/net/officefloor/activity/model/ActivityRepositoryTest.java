/*-
 * #%L
 * Activity
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

package net.officefloor.activity.model;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link ActivityRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link ActivityRepository}.
	 */
	private final ActivityRepository activityRepository = new ActivityRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link ActivityModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveActivity() throws Exception {

		// Create the raw Activity to be connected
		ActivityModel activity = new ActivityModel();
		ActivityInputModel input = new ActivityInputModel("INPUT", null);
		activity.addActivityInput(input);
		ActivitySectionModel section = new ActivitySectionModel("SECTION", null, null);
		activity.addActivitySection(section);
		ActivitySectionInputModel sectionInput = new ActivitySectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		ActivitySectionOutputModel sectionOutput = new ActivitySectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		ActivityProcedureModel procedure = new ActivityProcedureModel("PROCEDURE", null, null, null);
		activity.addActivityProcedure(procedure);
		ActivityProcedureOutputModel procedureOutput = new ActivityProcedureOutputModel("PROCEDURE_OUTPUT", null);
		procedure.addOutput(procedureOutput);
		ActivityProcedureNextModel procedureNext = new ActivityProcedureNextModel(null);
		procedure.setNext(procedureNext);
		ActivityExceptionModel exception = new ActivityExceptionModel("EXCEPTION");
		activity.addActivityException(exception);
		ActivityOutputModel output = new ActivityOutputModel("OUTPUT", null);
		activity.addActivityOutput(output);

		/*
		 * Input links
		 */

		// Input -> Section Input
		ActivityInputToActivitySectionInputModel inputToSectionInput = new ActivityInputToActivitySectionInputModel(
				"SECTION", "SECTION_INPUT");
		input.setActivitySectionInput(inputToSectionInput);

		// Input -> Procedure
		ActivityInputToActivityProcedureModel inputToProcedure = new ActivityInputToActivityProcedureModel("PROCEDURE");
		input.setActivityProcedure(inputToProcedure);

		// Input -> Output
		ActivityInputToActivityOutputModel inputToOutput = new ActivityInputToActivityOutputModel("OUTPUT");
		input.setActivityOutput(inputToOutput);

		/*
		 * Section Output links
		 */

		// Section Output -> Section Input
		ActivitySectionOutputToActivitySectionInputModel sectionOutputToSectionInput = new ActivitySectionOutputToActivitySectionInputModel(
				"SECTION", "SECTION_INPUT");
		sectionOutput.setActivitySectionInput(sectionOutputToSectionInput);

		// Section Output -> Procedure
		ActivitySectionOutputToActivityProcedureModel sectionOutputToProcedure = new ActivitySectionOutputToActivityProcedureModel(
				"PROCEDURE");
		sectionOutput.setActivityProcedure(sectionOutputToProcedure);

		// Section Output -> Output
		ActivitySectionOutputToActivityOutputModel sectionOutputToOutput = new ActivitySectionOutputToActivityOutputModel(
				"OUTPUT");
		sectionOutput.setActivityOutput(sectionOutputToOutput);

		/*
		 * Procedure next links
		 */

		// Procedure Next -> Section Input
		ActivityProcedureNextToActivitySectionInputModel procedureNextToSectionInput = new ActivityProcedureNextToActivitySectionInputModel(
				"SECTION", "SECTION_INPUT");
		procedureNext.setActivitySectionInput(procedureNextToSectionInput);

		// Procedure Next -> Procedure
		ActivityProcedureNextToActivityProcedureModel procedureNextToProcedure = new ActivityProcedureNextToActivityProcedureModel(
				"PROCEDURE");
		procedureNext.setActivityProcedure(procedureNextToProcedure);

		// Procedure Next -> Output
		ActivityProcedureNextToActivityOutputModel procedureNextToOutput = new ActivityProcedureNextToActivityOutputModel(
				"OUTPUT");
		procedureNext.setActivityOutput(procedureNextToOutput);

		/*
		 * Procedure output links
		 */

		// Procedure Output -> Section Input
		ActivityProcedureOutputToActivitySectionInputModel procedureOutputToSectionInput = new ActivityProcedureOutputToActivitySectionInputModel(
				"SECTION", "SECTION_INPUT");
		procedureOutput.setActivitySectionInput(procedureOutputToSectionInput);

		// Procedure Output -> Procedure
		ActivityProcedureOutputToActivityProcedureModel procedureOutputToProcedure = new ActivityProcedureOutputToActivityProcedureModel(
				"PROCEDURE");
		procedureOutput.setActivityProcedure(procedureOutputToProcedure);

		// Procedure Output -> Output
		ActivityProcedureOutputToActivityOutputModel procedureOutputToOutput = new ActivityProcedureOutputToActivityOutputModel(
				"OUTPUT");
		procedureOutput.setActivityOutput(procedureOutputToOutput);

		/*
		 * Exception links
		 */

		// Exception -> Section Input
		ActivityExceptionToActivitySectionInputModel exceptionToSectionInput = new ActivityExceptionToActivitySectionInputModel(
				"SECTION", "SECTION_INPUT");
		exception.setActivitySectionInput(exceptionToSectionInput);

		// Exception -> Procedure
		ActivityExceptionToActivityProcedureModel exceptionToProcedure = new ActivityExceptionToActivityProcedureModel(
				"PROCEDURE");
		exception.setActivityProcedure(exceptionToProcedure);

		// Exception -> Output
		ActivityExceptionToActivityOutputModel exceptionToOutput = new ActivityExceptionToActivityOutputModel("OUTPUT");
		exception.setActivityOutput(exceptionToOutput);

		// Record retrieving the Activity
		this.modelRepository.retrieve(this.paramType(ActivityModel.class), this.param(this.configurationItem));

		// Retrieve the Activity
		this.replayMockObjects();
		this.activityRepository.retrieveActivity(activity, this.configurationItem);
		this.verifyMockObjects();

		// Input links
		AssertLinks<ActivityInputModel> assertInput = new AssertLinks<>("input", input);
		assertInput.assertLink(inputToSectionInput, "section input", sectionInput);
		assertInput.assertLink(inputToProcedure, "procedure", procedure);
		assertInput.assertLink(inputToOutput, "output", output);

		// Section Output links
		AssertLinks<ActivitySectionOutputModel> assertSectionOutput = new AssertLinks<>("section output",
				sectionOutput);
		assertSectionOutput.assertLink(sectionOutputToSectionInput, "section input", sectionInput);
		assertSectionOutput.assertLink(sectionOutputToProcedure, "procedure", procedure);
		assertSectionOutput.assertLink(sectionOutputToOutput, "output", output);

		// Procedure Next links
		AssertLinks<ActivityProcedureNextModel> assertProcedureNext = new AssertLinks<>("procedure next",
				procedureNext);
		assertProcedureNext.assertLink(procedureNextToSectionInput, "section input", sectionInput);
		assertProcedureNext.assertLink(procedureNextToProcedure, "procedure", procedure);
		assertProcedureNext.assertLink(procedureNextToOutput, "output", output);

		// Procedure Output links
		AssertLinks<ActivityProcedureOutputModel> assertProcedureOutput = new AssertLinks<>("procedure output",
				procedureOutput);
		assertProcedureOutput.assertLink(procedureOutputToSectionInput, "section input", sectionInput);
		assertProcedureOutput.assertLink(procedureOutputToProcedure, "procedure", procedure);
		assertProcedureOutput.assertLink(procedureOutputToOutput, "output", output);

		// Exception links
		AssertLinks<ActivityExceptionModel> assertException = new AssertLinks<>("exception", exception);
		assertException.assertLink(exceptionToSectionInput, "section input", sectionInput);
		assertException.assertLink(exceptionToProcedure, "procedure", procedure);
		assertException.assertLink(exceptionToOutput, "output", output);
	}

	/**
	 * Convenience class to simplify asserting links.
	 */
	private static class AssertLinks<S extends Model> {

		private String sourceName;
		private S source;
		private String previousMethodName = null;

		private AssertLinks(String sourceName, S source) {
			this.sourceName = sourceName;
			this.source = source;
		}

		@SuppressWarnings("unchecked")
		private <L extends ConnectionModel, T extends Model> void assertLink(L link, String targetName, T target) {

			// Obtain the source
			this.previousMethodName = null;
			S linkSource = (S) this.getModel(link, this.source.getClass());
			T linkTarget = (T) this.getModel(link, target.getClass());

			// Undertake the assertions
			assertEquals(this.sourceName + " <- " + targetName, this.source, linkSource);
			assertEquals(this.sourceName + " -> " + targetName, target, linkTarget);
		}

		private <L extends ConnectionModel> Object getModel(L link, Class<?> modelType) {
			for (Method method : link.getClass().getMethods()) {
				if ((method.getReturnType() == modelType) && (!method.getName().equals(this.previousMethodName))) {
					try {
						return method.invoke(link);
					} catch (Exception ex) {
						throw fail(ex);
					}
				}
			}
			fail("Can not obtain link end model " + modelType.getName() + " from link " + link.getClass().getName());
			return null;
		}
	}

	/**
	 * Ensures on storing a {@link ActivityModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreActivity() throws Exception {

		// Create the Activity (without connections)
		ActivityModel woof = new ActivityModel();
		ActivityInputModel input = new ActivityInputModel("INPUT", null);
		woof.addActivityInput(input);
		ActivitySectionModel section = new ActivitySectionModel("SECTION", null, null);
		woof.addActivitySection(section);
		ActivitySectionInputModel sectionInput = new ActivitySectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		ActivitySectionOutputModel sectionOutput = new ActivitySectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		ActivityProcedureModel procedure = new ActivityProcedureModel("PROCEDURE", null, null, null);
		woof.addActivityProcedure(procedure);
		ActivityProcedureNextModel procedureNext = new ActivityProcedureNextModel();
		procedure.setNext(procedureNext);
		ActivityProcedureOutputModel procedureOutput = new ActivityProcedureOutputModel("PROCEDURE OUTPUT", null);
		procedure.addOutput(procedureOutput);
		ActivityExceptionModel exception = new ActivityExceptionModel("EXCEPTION");
		woof.addActivityException(exception);
		ActivityOutputModel output = new ActivityOutputModel("OUTPUT", null);
		woof.addActivityOutput(output);

		// Input links
		ActivityInputToActivitySectionInputModel inputToSectionInput = link(
				new ActivityInputToActivitySectionInputModel(), input, sectionInput);
		ActivityInputToActivityProcedureModel inputToProcedure = link(new ActivityInputToActivityProcedureModel(),
				input, procedure);
		ActivityInputToActivityOutputModel inputToOutput = link(new ActivityInputToActivityOutputModel(), input,
				output);

		// Section Output links
		ActivitySectionOutputToActivitySectionInputModel sectionOutputToSectionInput = link(
				new ActivitySectionOutputToActivitySectionInputModel(), sectionOutput, sectionInput);
		ActivitySectionOutputToActivityProcedureModel sectionOutputToProcedure = link(
				new ActivitySectionOutputToActivityProcedureModel(), sectionOutput, procedure);
		ActivitySectionOutputToActivityOutputModel sectionOutputToOutput = link(
				new ActivitySectionOutputToActivityOutputModel(), sectionOutput, output);

		// Procedure Next links
		ActivityProcedureNextToActivitySectionInputModel procedureNextToSectionInput = link(
				new ActivityProcedureNextToActivitySectionInputModel(), procedureNext, sectionInput);
		ActivityProcedureNextToActivityProcedureModel procedureNextToProcedure = link(
				new ActivityProcedureNextToActivityProcedureModel(), procedureNext, procedure);
		ActivityProcedureNextToActivityOutputModel procedureNextToOutput = link(
				new ActivityProcedureNextToActivityOutputModel(), procedureNext, output);

		// Procedure Output links
		ActivityProcedureOutputToActivitySectionInputModel procedureOutputToSectionInput = link(
				new ActivityProcedureOutputToActivitySectionInputModel(), procedureOutput, sectionInput);
		ActivityProcedureOutputToActivityProcedureModel procedureOutputToProcedure = link(
				new ActivityProcedureOutputToActivityProcedureModel(), procedureOutput, procedure);
		ActivityProcedureOutputToActivityOutputModel procedureOutputToOutput = link(
				new ActivityProcedureOutputToActivityOutputModel(), procedureOutput, output);

		// Exception links
		ActivityExceptionToActivitySectionInputModel exceptionToSectionInput = link(
				new ActivityExceptionToActivitySectionInputModel(), exception, sectionInput);
		ActivityExceptionToActivityProcedureModel exceptionToProcedure = link(
				new ActivityExceptionToActivityProcedureModel(), exception, procedure);
		ActivityExceptionToActivityOutputModel exceptionToOutput = link(new ActivityExceptionToActivityOutputModel(),
				exception, output);

		// Record storing the Activity
		this.modelRepository.store(woof, this.configurationItem);

		// Store the Activity
		this.replayMockObjects();
		this.activityRepository.storeActivity(woof, this.configurationItem);
		this.verifyMockObjects();

		// Assert Input links
		assertEquals("input - section input (section name)", "SECTION", inputToSectionInput.getSectionName());
		assertEquals("input - section input (input name)", "SECTION_INPUT", inputToSectionInput.getInputName());
		assertEquals("input - procedure", "PROCEDURE", inputToProcedure.getProcedureName());
		assertEquals("input - output", "OUTPUT", inputToOutput.getOutputName());

		// Assert Section Output links
		assertEquals("section output - section input (section name)", "SECTION",
				sectionOutputToSectionInput.getSectionName());
		assertEquals("section output - section input (input name)", "SECTION_INPUT",
				sectionOutputToSectionInput.getInputName());
		assertEquals("section output - procedure", "PROCEDURE", sectionOutputToProcedure.getProcedureName());
		assertEquals("section output - output", "OUTPUT", sectionOutputToOutput.getOutputName());

		// Assert Procedure Next links
		assertEquals("procedure next - section input (section name)", "SECTION",
				procedureNextToSectionInput.getSectionName());
		assertEquals("procedure next - section input (input name)", "SECTION_INPUT",
				procedureNextToSectionInput.getInputName());
		assertEquals("procedure next - procedure", "PROCEDURE", procedureNextToProcedure.getProcedureName());
		assertEquals("procedure next - output", "OUTPUT", procedureNextToOutput.getOutputName());

		// Assert Procedure Output links
		assertEquals("procedure output - section input (section name)", "SECTION",
				procedureOutputToSectionInput.getSectionName());
		assertEquals("procedure output - section input (input name)", "SECTION_INPUT",
				procedureOutputToSectionInput.getInputName());
		assertEquals("procedure output - procedure", "PROCEDURE", procedureOutputToProcedure.getProcedureName());
		assertEquals("procedure output - output", "OUTPUT", procedureOutputToOutput.getOutputName());

		// Assert Exception links
		assertEquals("exception - section input (section name)", "SECTION", exceptionToSectionInput.getSectionName());
		assertEquals("exception - section input (input name)", "SECTION_INPUT", exceptionToSectionInput.getInputName());
		assertEquals("exception - procedure", "PROCEDURE", exceptionToProcedure.getProcedureName());
		assertEquals("exception - output", "OUTPUT", exceptionToOutput.getOutputName());
	}

	/**
	 * Convenience method to create a link.
	 */
	private static <L extends ConnectionModel> L link(L link, Model source, Model target) {
		final Closure<String> previousMethodName = new Closure<>();
		final Consumer<Model> loadEndModel = (model) -> {
			for (Method method : link.getClass().getMethods()) {
				if ((method.getParameterTypes().length == 1) && (method.getParameterTypes()[0] == model.getClass())
						&& (!method.getName().equals(previousMethodName.value))) {
					try {
						method.invoke(link, model);
					} catch (Exception ex) {
						throw fail(ex);
					}
					previousMethodName.value = method.getName();
					return; // loaded
				}
			}
			fail("Unable to set model " + model.getClass().getName() + " on connection " + link.getClass().getName());
		};
		loadEndModel.accept(source);
		loadEndModel.accept(target);
		link.connect();
		return link;
	}

}
