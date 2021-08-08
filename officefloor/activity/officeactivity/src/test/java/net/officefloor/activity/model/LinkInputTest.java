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
 * Tests linking from the {@link ActivityInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkInputTest extends AbstractActivityChangesTestCase {

	/**
	 * Index of {@link ActivityInputModel} A.
	 */
	private static final int A = 0;

	/**
	 * Index of {@link ActivityInputModel} B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link ActivitySectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityInputToActivitySectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivitySectionInputModel}.
	 * 
	 * @param index {@link ActivityInputModel} index.
	 */
	private void doLinkToSectionInput(int index) {

		// Obtain the items to link
		ActivityInputModel input = this.model.getActivityInputs().get(index);
		ActivitySectionInputModel sectionInput = this.model.getActivitySections().get(B).getInputs().get(0);

		// Link to section input
		Change<ActivityInputToActivitySectionInputModel> change = this.operations.linkInputToSectionInput(input,
				sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Input to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link ActivityInputToActivitySectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		ActivityInputToActivitySectionInputModel link = this.model.getActivityInputs().get(B).getActivitySectionInput();

		// Remove the link
		Change<ActivityInputToActivitySectionInputModel> change = this.operations.removeInputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Input to Section Input", true);
	}

	/**
	 * Ensure can link to {@link ActivityProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityInputToActivityProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityProcedureModel}.
	 * 
	 * @param index {@link ActivityInputModel} index.
	 */
	private void doLinkToProcedure(int index) {

		// Obtain the items to link
		ActivityInputModel input = this.model.getActivityInputs().get(index);
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(1);

		// Link to procedure
		Change<ActivityInputToActivityProcedureModel> change = this.operations.linkInputToProcedure(input, procedure);

		// Validate change
		this.assertChange(change, null, "Link Input to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link ActivityInputToActivityProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		ActivityInputToActivityProcedureModel link = this.model.getActivityInputs().get(B).getActivityProcedure();

		// Remove the link
		Change<ActivityInputToActivityProcedureModel> change = this.operations.removeInputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Input to Procedure", true);
	}

	/**
	 * Ensure can link to {@link ActivityOutputModel}.
	 */
	public void testLinkToOutput() {
		this.doLinkToOutput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityAccessOutputToActivityOutputModel}.
	 */
	public void testLinkOverrideToOutput() {
		this.doLinkToOutput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityOutputModel}.
	 * 
	 * @param index {@link ActivityInputModel} index.
	 */
	private void doLinkToOutput(int index) {

		// Obtain the items to link
		ActivityInputModel input = this.model.getActivityInputs().get(index);
		ActivityOutputModel output = this.model.getActivityOutputs().get(1);

		// Link to output
		Change<ActivityInputToActivityOutputModel> change = this.operations.linkInputToOutput(input, output);

		// Validate change
		this.assertChange(change, null, "Link Input to Output", true);
	}

	/**
	 * Ensure can remove the {@link ActivityInputToActivityOutputModel}.
	 */
	public void testRemoveToOutput() {

		// Obtain the link to remove
		ActivityInputToActivityOutputModel link = this.model.getActivityInputs().get(B).getActivityOutput();

		// Remove the link
		Change<ActivityInputToActivityOutputModel> change = this.operations.removeInputToOutput(link);

		// Validate change
		this.assertChange(change, null, "Remove Input to Output", true);
	}

}
