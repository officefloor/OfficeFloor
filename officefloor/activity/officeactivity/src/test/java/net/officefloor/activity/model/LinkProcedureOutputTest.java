/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.model;

import net.officefloor.model.change.Change;

/**
 * Tests linking from the {@link ActivityProcedureOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkProcedureOutputTest extends AbstractActivityChangesTestCase {

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
	 * {@link ActivityProcedureOutputToActivityProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link ActivityProcedureModel}.
	 * 
	 * @param procedureIndex {@link ActivityProcedureOutputModel} index.
	 */
	private void doLinkToProcedure(int procedureIndex) {

		// Obtain the items to link
		ActivityProcedureOutputModel procedureOutput = this.model.getActivityProcedures().get(procedureIndex)
				.getOutputs().get(0);
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(1);

		// Link the procedure output to procedure
		Change<ActivityProcedureOutputToActivityProcedureModel> change = this.operations
				.linkProcedureOutputToProcedure(procedureOutput, procedure);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Procedure", true);
	}

	/**
	 * Ensure can remove the
	 * {@link ActivityProcedureOutputToActivityProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		ActivityProcedureOutputToActivityProcedureModel link = this.model.getActivityProcedures().get(B).getOutputs()
				.get(0).getActivityProcedure();

		// Remove the link
		Change<ActivityProcedureOutputToActivityProcedureModel> change = this.operations
				.removeProcedureOutputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Procedure", true);
	}

	/**
	 * Ensure can link to {@link ActivitySectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityProcedureOutputToActivitySectionInputModel}.
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
		ActivityProcedureOutputModel procedureOutput = this.model.getActivityProcedures().get(procedureIndex)
				.getOutputs().get(0);
		ActivitySectionInputModel sectionInput = this.model.getActivitySections().get(B).getInputs().get(0);

		// Link the procedure output to section input
		Change<ActivityProcedureOutputToActivitySectionInputModel> change = this.operations
				.linkProcedureOutputToSectionInput(procedureOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Section Input", true);
	}

	/**
	 * Ensure can remove the
	 * {@link ActivityProcedureOutputToActivitySectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		ActivityProcedureOutputToActivitySectionInputModel link = this.model.getActivityProcedures().get(B).getOutputs()
				.get(0).getActivitySectionInput();

		// Remove the link
		Change<ActivityProcedureOutputToActivitySectionInputModel> change = this.operations
				.removeProcedureOutputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link ActivityOutputModel}.
	 */
	public void testLinkToOutput() {
		this.doLinkToOutput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link ActivityTemplateOutputToActivityOutputModel}.
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
		ActivityProcedureOutputModel procedureOutput = this.model.getActivityProcedures().get(procedureIndex)
				.getOutputs().get(0);
		ActivityOutputModel output = this.model.getActivityOutputs().get(1);

		// Link the template output to resource
		Change<ActivityProcedureOutputToActivityOutputModel> change = this.operations
				.linkProcedureOutputToOutput(procedureOutput, output);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Output", true);
	}

	/**
	 * Ensure can remove the {@link ActivityProcedureOutputToActivityOutputModel}.
	 */
	public void testRemoveToOutput() {

		// Obtain the link to remove
		ActivityProcedureOutputToActivityOutputModel link = this.model.getActivityProcedures().get(B).getOutputs()
				.get(0).getActivityOutput();

		// Remove the link
		Change<ActivityProcedureOutputToActivityOutputModel> change = this.operations
				.removeProcedureOutputToOutput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Output", true);
	}

}