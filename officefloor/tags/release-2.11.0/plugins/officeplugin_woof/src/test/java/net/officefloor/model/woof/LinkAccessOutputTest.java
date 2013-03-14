/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
 * Tests linking from the {@link WoofAccessOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkAccessOutputTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of {@link WoofAccessOutputModel} A.
	 */
	private static final int A = 0;

	/**
	 * Index of {@link WoofAccessOutputModel} B.
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
	 * {@link WoofAccessOutputToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param accessIndex
	 *            {@link WoofAccessOutputModel} index.
	 */
	private void doLinkToTemplate(int accessIndex) {

		// Obtain the items to link
		WoofAccessOutputModel accessOutput = this.model.getWoofAccess()
				.getOutputs().get(accessIndex);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the access output to template
		Change<WoofAccessOutputToWoofTemplateModel> change = this.operations
				.linkAccessOutputToTemplate(accessOutput, template);

		// Validate change
		this.assertChange(change, null, "Link Access Output to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofAccessOutputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplateLink() {

		// Obtain the link to remove
		WoofAccessOutputToWoofTemplateModel link = this.model.getWoofAccess()
				.getOutputs().get(B).getWoofTemplate();

		// Remove the link
		Change<WoofAccessOutputToWoofTemplateModel> change = this.operations
				.removeAccessOuputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Access Output to Template",
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
	 * {@link WoofAccessOutputToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param accessIndex
	 *            {@link WoofAccessOutputModel} index.
	 */
	private void doLinkToSectionInput(int accessIndex) {

		// Obtain the items to link
		WoofAccessOutputModel accessOutput = this.model.getWoofAccess()
				.getOutputs().get(accessIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(B).getInputs().get(0);

		// Link the access output to section input
		Change<WoofAccessOutputToWoofSectionInputModel> change = this.operations
				.linkAccessOutputToSectionInput(accessOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Access Output to Section Input",
				true);
	}

	/**
	 * Ensure can remove the {@link WoofAccessOutputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofAccessOutputToWoofSectionInputModel link = this.model
				.getWoofAccess().getOutputs().get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofAccessOutputToWoofSectionInputModel> change = this.operations
				.removeAccessOuputToSectionInput(link);

		// Validate change
		this.assertChange(change, null,
				"Remove Access Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofResourceModel}.
	 */
	public void testLinkToResource() {
		this.doLinkToResource(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofAccessOutputToWoofResourceModel}.
	 */
	public void testLinkOverrideToResource() {
		this.doLinkToResource(B);
	}

	/**
	 * Undertakes linking to a {@link WoofResourceModel}.
	 * 
	 * @param accessIndex
	 *            {@link WoofAccessOutputModel} index.
	 */
	private void doLinkToResource(int accessIndex) {

		// Obtain the items to link
		WoofAccessOutputModel accessOutput = this.model.getWoofAccess()
				.getOutputs().get(accessIndex);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the access output to resource
		Change<WoofAccessOutputToWoofResourceModel> change = this.operations
				.linkAccessOutputToResource(accessOutput, resource);

		// Validate change
		this.assertChange(change, null, "Link Access Output to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofAccessOutputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofAccessOutputToWoofResourceModel link = this.model.getWoofAccess()
				.getOutputs().get(B).getWoofResource();

		// Remove the link
		Change<WoofAccessOutputToWoofResourceModel> change = this.operations
				.removeAccessOuputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Access Output to Resource",
				true);
	}

}