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
package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Tests linking from the {@link WoofAccessOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkSecurityOutputTest extends AbstractWoofChangesTestCase {

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
	 * @param securityIndex
	 *            {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToTemplate(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(0).getOutputs().get(securityIndex);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the security output to template
		Change<WoofSecurityOutputToWoofTemplateModel> change = this.operations
				.linkSecurityOutputToTemplate(securityOutput, template);

		// Validate change
		this.assertChange(change, null, "Link Security Output to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplateLink() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofTemplateModel link = this.model.getWoofSecurities().get(0).getOutputs().get(B)
				.getWoofTemplate();

		// Remove the link
		Change<WoofSecurityOutputToWoofTemplateModel> change = this.operations.removeSecurityOuputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Template", true);
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
	 * @param securityIndex
	 *            {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToSectionInput(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(0).getOutputs().get(securityIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link the security output to section input
		Change<WoofSecurityOutputToWoofSectionInputModel> change = this.operations
				.linkSecurityOutputToSectionInput(securityOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Security Output to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofSectionInputModel link = this.model.getWoofSecurities().get(0).getOutputs().get(B)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofSecurityOutputToWoofSectionInputModel> change = this.operations
				.removeSecurityOuputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Section Input", true);
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
	 * @param securityIndex
	 *            {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToResource(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel accessOutput = this.model.getWoofSecurities().get(0).getOutputs().get(securityIndex);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the security output to resource
		Change<WoofSecurityOutputToWoofResourceModel> change = this.operations
				.linkSecurityOutputToResource(accessOutput, resource);

		// Validate change
		this.assertChange(change, null, "Link Security Output to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofResourceModel link = this.model.getWoofSecurities().get(0).getOutputs().get(B)
				.getWoofResource();

		// Remove the link
		Change<WoofSecurityOutputToWoofResourceModel> change = this.operations.removeSecurityOuputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Resource", true);
	}

}