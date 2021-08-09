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
 * Tests linking from the {@link ActivityExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkExceptionTest extends AbstractActivityChangesTestCase {

	/**
	 * Index of exception A.
	 */
	private static final int A = 0;

	/**
	 * Index of exception B.
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
	 * {@link ActivityExceptionToActivityProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityProcedureModel}.
	 * 
	 * @param exceptionIndex {@link ActivityExceptionModel} index.
	 */
	private void doLinkToProcedure(int exceptionIndex) {

		// Obtain the items to link
		ActivityExceptionModel exception = this.model.getActivityExceptions().get(exceptionIndex);
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(1);

		// Link the exception to procedure
		Change<ActivityExceptionToActivityProcedureModel> change = this.operations.linkExceptionToProcedure(exception,
				procedure);

		// Validate change
		this.assertChange(change, null, "Link Exception to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link ActivityExceptionToActivityProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		ActivityExceptionToActivityProcedureModel link = this.model.getActivityExceptions().get(B)
				.getActivityProcedure();

		// Remove the link
		Change<ActivityExceptionToActivityProcedureModel> change = this.operations.removeExceptionToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Procedure", true);
	}

	/**
	 * Ensure can link to {@link ActivitySectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityExceptionToActivitySectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivitySectionInputModel}.
	 * 
	 * @param exceptionIndex {@link ActivityExceptionModel} index.
	 */
	private void doLinkToSectionInput(int exceptionIndex) {

		// Obtain the items to link
		ActivityExceptionModel exception = this.model.getActivityExceptions().get(exceptionIndex);
		ActivitySectionInputModel sectionInput = this.model.getActivitySections().get(1).getInputs().get(0);

		// Link the exception to section input
		Change<ActivityExceptionToActivitySectionInputModel> change = this.operations
				.linkExceptionToSectionInput(exception, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Exception to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link ActivityExceptionToActivitySectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		ActivityExceptionToActivitySectionInputModel link = this.model.getActivityExceptions().get(B)
				.getActivitySectionInput();

		// Remove the link
		Change<ActivityExceptionToActivitySectionInputModel> change = this.operations
				.removeExceptionToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Section Input", true);
	}

	/**
	 * Ensure can link to {@link ActivityOutputModel}.
	 */
	public void testLinkToOutput() {
		this.doLinkToOutput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityExceptionToActivityOutputModel}.
	 */
	public void testLinkOverrideToOutput() {
		this.doLinkToOutput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityOutputModel}.
	 * 
	 * @param exceptionIndex {@link ActivityExceptionModel} index.
	 */
	private void doLinkToOutput(int exceptionIndex) {

		// Obtain the items to link
		ActivityExceptionModel exception = this.model.getActivityExceptions().get(exceptionIndex);
		ActivityOutputModel output = this.model.getActivityOutputs().get(1);

		// Link the exception to output
		Change<ActivityExceptionToActivityOutputModel> change = this.operations.linkExceptionToOutput(exception,
				output);

		// Validate change
		this.assertChange(change, null, "Link Exception to Output", true);
	}

	/**
	 * Ensure can remove the {@link ActivityExceptionToActivityOutputModel}.
	 */
	public void testRemoveToOutput() {

		// Obtain the link to remove
		ActivityExceptionToActivityOutputModel link = this.model.getActivityExceptions().get(B).getActivityOutput();

		// Remove the link
		Change<ActivityExceptionToActivityOutputModel> change = this.operations.removeExceptionToOutput(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Output", true);
	}

}
