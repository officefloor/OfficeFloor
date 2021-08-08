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

import net.officefloor.model.change.Change;

/**
 * Tests linking from the {@link ActivitySectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkSectionOutputTest extends AbstractActivityChangesTestCase {

	/**
	 * Index of section A.
	 */
	private static final int A = 0;

	/**
	 * Index of section B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link ActivityProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivitySectionOutputToActivityProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityProcedureModel}.
	 * 
	 * @param sectionIndex {@link ActivitySectionOutputModel} index.
	 */
	private void doLinkToProcedure(int sectionIndex) {

		// Obtain the items to link
		ActivitySectionOutputModel sectionOutput = this.model.getActivitySections().get(sectionIndex).getOutputs()
				.get(0);
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(1);

		// Link the section output to procedure
		Change<ActivitySectionOutputToActivityProcedureModel> change = this.operations
				.linkSectionOutputToProcedure(sectionOutput, procedure);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link ActivitySectionOutputToActivityProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		ActivitySectionOutputToActivityProcedureModel link = this.model.getActivitySections().get(B).getOutputs().get(0)
				.getActivityProcedure();

		// Remove the link
		Change<ActivitySectionOutputToActivityProcedureModel> change = this.operations
				.removeSectionOutputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Procedure", true);
	}

	/**
	 * Ensure can link to {@link ActivitySectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivitySectionOutputToActivitySectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivitySectionInputModel}.
	 * 
	 * @param sectionIndex {@link ActivitySectionModel} index.
	 */
	private void doLinkToSectionInput(int sectionIndex) {

		// Obtain the items to link
		ActivitySectionOutputModel sectionOutput = this.model.getActivitySections().get(sectionIndex).getOutputs()
				.get(0);
		ActivitySectionInputModel sectionInput = this.model.getActivitySections().get(B).getInputs().get(0);

		// Link the template output to section input
		Change<ActivitySectionOutputToActivitySectionInputModel> change = this.operations
				.linkSectionOutputToSectionInput(sectionOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Section Input", true);
	}

	/**
	 * Ensure can remove the
	 * {@link ActivitySectionOutputToActivitySectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		ActivitySectionOutputToActivitySectionInputModel link = this.model.getActivitySections().get(B).getOutputs()
				.get(0).getActivitySectionInput();

		// Remove the link
		Change<ActivitySectionOutputToActivitySectionInputModel> change = this.operations
				.removeSectionOutputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link ActivityOutputModel}.
	 */
	public void testLinkToOutput() {
		this.doLinkToOutput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivitySectionOutputToActivityOutputModel}.
	 */
	public void testLinkOverrideToOutput() {
		this.doLinkToOutput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityOutputModel}.
	 * 
	 * @param sectionIndex {@link ActivitySectionModel} index.
	 */
	private void doLinkToOutput(int sectionIndex) {

		// Obtain the items to link
		ActivitySectionOutputModel sectionOutput = this.model.getActivitySections().get(sectionIndex).getOutputs()
				.get(0);
		ActivityOutputModel output = this.model.getActivityOutputs().get(1);

		// Link the template output to resource
		Change<ActivitySectionOutputToActivityOutputModel> change = this.operations
				.linkSectionOutputToOutput(sectionOutput, output);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Output", true);
	}

	/**
	 * Ensure can remove the {@link ActivitySectionOutputToActivityOutputModel}.
	 */
	public void testRemoveToOutput() {

		// Obtain the link to remove
		ActivitySectionOutputToActivityOutputModel link = this.model.getActivitySections().get(B).getOutputs().get(0)
				.getActivityOutput();

		// Remove the link
		Change<ActivitySectionOutputToActivityOutputModel> change = this.operations.removeSectionOutputToOutput(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Output", true);
	}

}
