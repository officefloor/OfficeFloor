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