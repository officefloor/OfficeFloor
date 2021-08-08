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
 * Tests linking from the {@link ActivityProcedureNextModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkProcedureNextTest extends AbstractActivityChangesTestCase {

	/**
	 * Index of procedure A.
	 */
	private static final int A = 0;

	/**
	 * Index of procedure B.
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
	 * {@link ActivityProcedureNextToActivityProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityProcedureModel}.
	 * 
	 * @param procedureIndex {@link ActivityProcedureNextModel} index.
	 */
	private void doLinkToProcedure(int procedureIndex) {

		// Obtain the items to link
		ActivityProcedureNextModel procedureNext = this.model.getActivityProcedures().get(procedureIndex).getNext();
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(1);

		// Link the procedure output to procedure
		Change<ActivityProcedureNextToActivityProcedureModel> change = this.operations
				.linkProcedureNextToProcedure(procedureNext, procedure);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link ActivityProcedureNextToActivityProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		ActivityProcedureNextToActivityProcedureModel link = this.model.getActivityProcedures().get(B).getNext()
				.getActivityProcedure();

		// Remove the link
		Change<ActivityProcedureNextToActivityProcedureModel> change = this.operations
				.removeProcedureNextToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Procedure", true);
	}

	/**
	 * Ensure can link to {@link ActivitySectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityProcedureNextToActivitySectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivitySectionInputModel}.
	 * 
	 * @param procedureIndex {@link ActivityProcedureModel} index.
	 */
	private void doLinkToSectionInput(int procedureIndex) {

		// Obtain the items to link
		ActivityProcedureNextModel procedureNext = this.model.getActivityProcedures().get(procedureIndex).getNext();
		ActivitySectionInputModel sectionInput = this.model.getActivitySections().get(B).getInputs().get(0);

		// Link the procedure output to section input
		Change<ActivityProcedureNextToActivitySectionInputModel> change = this.operations
				.linkProcedureNextToSectionInput(procedureNext, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Section Input", true);
	}

	/**
	 * Ensure can remove the
	 * {@link ActivityProcedureNextToActivitySectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		ActivityProcedureNextToActivitySectionInputModel link = this.model.getActivityProcedures().get(B).getNext()
				.getActivitySectionInput();

		// Remove the link
		Change<ActivityProcedureNextToActivitySectionInputModel> change = this.operations
				.removeProcedureNextToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Section Input", true);
	}

	/**
	 * Ensure can link to {@link ActivityOutputModel}.
	 */
	public void testLinkToOutput() {
		this.doLinkToOutput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityProcedureNextToActivityOutputModel}.
	 */
	public void testLinkOverrideToOutput() {
		this.doLinkToOutput(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityOutputModel}.
	 * 
	 * @param procedureIndex {@link ActivityProcedureModel} index.
	 */
	private void doLinkToOutput(int procedureIndex) {

		// Obtain the items to link
		ActivityProcedureNextModel procedureNext = this.model.getActivityProcedures().get(procedureIndex).getNext();
		ActivityOutputModel output = this.model.getActivityOutputs().get(1);

		// Link the template output to resource
		Change<ActivityProcedureNextToActivityOutputModel> change = this.operations
				.linkProcedureNextToOutput(procedureNext, output);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Output", true);
	}

	/**
	 * Ensure can remove the {@link ActivityProcedureNextToActivityOutputModel}.
	 */
	public void testRemoveToOutput() {

		// Obtain the link to remove
		ActivityProcedureNextToActivityOutputModel link = this.model.getActivityProcedures().get(B).getNext()
				.getActivityOutput();

		// Remove the link
		Change<ActivityProcedureNextToActivityOutputModel> change = this.operations.removeProcedureNextToOutput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Output", true);
	}

}
