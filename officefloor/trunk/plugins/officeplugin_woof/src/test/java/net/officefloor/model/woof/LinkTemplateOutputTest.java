/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests linking from the {@link WoofTemplateOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkTemplateOutputTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of template A.
	 */
	private static final int A = 0;

	/**
	 * Index of template B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link WoofTemplateModel}.
	 */
	public void testLinkToTemplate() {
		this.doLinkToTemplate(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofTemplateOutputToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param templateIndex
	 *            {@link WoofTemplateModel} index.
	 */
	private void doLinkToTemplate(int templateIndex) {

		// Obtain the items to link
		WoofTemplateOutputModel templateOutput = this.model.getWoofTemplates()
				.get(templateIndex).getOutputs().get(0);
		WoofTemplateModel template = this.model.getWoofTemplates().get(B);

		// Link the template output to template
		Change<WoofTemplateOutputToWoofTemplateModel> change = this.operations
				.linkTemplateOutputToTemplate(templateOutput, template);

		// Validate change
		this.assertChange(change, null, "Link Template Output to Template",
				true);
	}

	/**
	 * Ensure can remove the {@link WoofTemplateOutputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplateLink() {

		// Obtain the link to remove
		WoofTemplateOutputToWoofTemplateModel link = this.model
				.getWoofTemplates().get(B).getOutputs().get(0)
				.getWoofTemplate();

		// Remove the link
		Change<WoofTemplateOutputToWoofTemplateModel> change = this.operations
				.removeTemplateOuputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Template Output to Template",
				true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofTemplateOutputToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param templateIndex
	 *            {@link WoofTemplateModel} index.
	 */
	private void doLinkToSectionInput(int templateIndex) {

		// Obtain the items to link
		WoofTemplateOutputModel templateOutput = this.model.getWoofTemplates()
				.get(templateIndex).getOutputs().get(0);
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(1).getInputs().get(0);

		// Link the template output to section input
		Change<WoofTemplateOutputToWoofSectionInputModel> change = this.operations
				.linkTemplateOutputToSectionInput(templateOutput, sectionInput);

		// Validate change
		this.assertChange(change, null,
				"Link Template Output to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofTemplateOutputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofTemplateOutputToWoofSectionInputModel link = this.model
				.getWoofTemplates().get(B).getOutputs().get(0)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofTemplateOutputToWoofSectionInputModel> change = this.operations
				.removeTemplateOuputToSectionInput(link);

		// Validate change
		this.assertChange(change, null,
				"Remove Template Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofResourceModel}.
	 */
	public void testLinkToResource() {
		this.doLinkToResource(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofTemplateOutputToWoofResourceModel}.
	 */
	public void testLinkOverrideToResource() {
		this.doLinkToResource(B);
	}

	/**
	 * Undertakes linking to a {@link WoofResourceModel}.
	 * 
	 * @param templateIndex
	 *            {@link WoofTemplateModel} index.
	 */
	private void doLinkToResource(int templateIndex) {

		// Obtain the items to link
		WoofTemplateOutputModel templateOutput = this.model.getWoofTemplates()
				.get(templateIndex).getOutputs().get(0);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the template output to resource
		Change<WoofTemplateOutputToWoofResourceModel> change = this.operations
				.linkTemplateOutputToResource(templateOutput, resource);

		// Validate change
		this.assertChange(change, null, "Link Template Output to Resource",
				true);
	}

	/**
	 * Ensure can remove the {@link WoofTemplateOutputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofTemplateOutputToWoofResourceModel link = this.model
				.getWoofTemplates().get(B).getOutputs().get(0)
				.getWoofResource();

		// Remove the link
		Change<WoofTemplateOutputToWoofResourceModel> change = this.operations
				.removeTemplateOuputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Template Output to Resource",
				true);
	}

}