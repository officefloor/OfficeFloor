/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
 * Tests linking from the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkExceptionTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of exception A.
	 */
	private static final int A = 0;

	/**
	 * Index of exception B.
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
	 * {@link WoofExceptionToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param exceptionIndex
	 *            {@link WoofExceptionModel} index.
	 */
	private void doLinkToTemplate(int exceptionIndex) {

		// Obtain the items to link
		WoofExceptionModel exception = this.model.getWoofExceptions().get(
				exceptionIndex);
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);

		// Link the exception to template
		Change<WoofExceptionToWoofTemplateModel> change = this.operations
				.linkExceptionToTemplate(exception, template);

		// Validate change
		this.assertChange(change, null, "Link Exception to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofTemplateModel}.
	 */
	public void testRemoveToTemplateLink() {

		// Obtain the link to remove
		WoofExceptionToWoofTemplateModel link = this.model.getWoofExceptions()
				.get(B).getWoofTemplate();

		// Remove the link
		Change<WoofExceptionToWoofTemplateModel> change = this.operations
				.removeExceptionToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofExceptionToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param exceptionIndex
	 *            {@link WoofExceptionModel} index.
	 */
	private void doLinkToSectionInput(int exceptionIndex) {

		// Obtain the items to link
		WoofExceptionModel exception = this.model.getWoofExceptions().get(
				exceptionIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(B).getInputs().get(0);

		// Link the exception to section input
		Change<WoofExceptionToWoofSectionInputModel> change = this.operations
				.linkExceptionToSectionInput(exception, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Exception to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofExceptionToWoofSectionInputModel link = this.model
				.getWoofExceptions().get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofExceptionToWoofSectionInputModel> change = this.operations
				.removeExceptionToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Section Input",
				true);
	}

	/**
	 * Ensure can link to {@link WoofResourceModel}.
	 */
	public void testLinkToResource() {
		this.doLinkToResource(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofExceptionToWoofResourceModel}.
	 */
	public void testLinkOverrideToResource() {
		this.doLinkToResource(B);
	}

	/**
	 * Undertakes linking to a {@link WoofResourceModel}.
	 * 
	 * @param exceptionIndex
	 *            {@link WoofExceptionModel} index.
	 */
	private void doLinkToResource(int exceptionIndex) {

		// Obtain the items to link
		WoofExceptionModel exception = this.model.getWoofExceptions().get(
				exceptionIndex);
		WoofResourceModel resource = this.model.getWoofResources().get(0);

		// Link the exception to resource
		Change<WoofExceptionToWoofResourceModel> change = this.operations
				.linkExceptionToResource(exception, resource);

		// Validate change
		this.assertChange(change, null, "Link Exception to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofExceptionToWoofResourceModel link = this.model.getWoofExceptions()
				.get(B).getWoofResource();

		// Remove the link
		Change<WoofExceptionToWoofResourceModel> change = this.operations
				.removeExceptionToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Resource", true);
	}

}