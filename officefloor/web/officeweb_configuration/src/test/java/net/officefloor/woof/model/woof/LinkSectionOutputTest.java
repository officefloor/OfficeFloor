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
package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;

/**
 * Tests linking from the {@link WoofSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkSectionOutputTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of section A.
	 */
	private static final int A = 0;

	/**
	 * Index of section B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link WoofHttpContinuationModel}.
	 */
	public void testLinkToHttpContinuation() {
		this.doLinkToHttpContinuation(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSectionOutputToWoofHttpContinuationModel}.
	 */
	public void testLinkOverrideToHttpContinuation() {
		this.doLinkToHttpContinuation(B);
	}

	/**
	 * Undertakes linking to a {@link WoofHttpContinuationModel}.
	 * 
	 * @param sectionIndex
	 *            {@link WoofSectionModel} index.
	 */
	private void doLinkToHttpContinuation(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(1);

		// Link the section output to HTTP continuation
		Change<WoofSectionOutputToWoofHttpContinuationModel> change = this.operations
				.linkSectionOutputToHttpContinuation(sectionOutput, httpContinuation);

		// Validate change
		this.assertChange(change, null, "Link Section Output to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the
	 * {@link WoofSectionOutputToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofSectionOutputToWoofHttpContinuationModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofHttpContinuation();

		// Remove the link
		Change<WoofSectionOutputToWoofHttpContinuationModel> change = this.operations
				.removeSectionOuputToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to HTTP Continuation", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionModel}.
	 */
	public void testLinkToTemplate() {
		this.doLinkToTemplate(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSectionOutputToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param sectionIndex
	 *            {@link WoofSectionModel} index.
	 */
	private void doLinkToTemplate(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the section output to template
		Change<WoofSectionOutputToWoofTemplateModel> change = this.operations.linkSectionOutputToTemplate(sectionOutput,
				template);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofSectionOutputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofSectionOutputToWoofTemplateModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofTemplate();

		// Remove the link
		Change<WoofSectionOutputToWoofTemplateModel> change = this.operations.removeSectionOuputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Template", true);
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
	 * @param sectionIndex
	 *            {@link WoofSectionModel} index.
	 */
	private void doLinkToSectionInput(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link the template output to section input
		Change<WoofSectionOutputToWoofSectionInputModel> change = this.operations
				.linkSectionOutputToSectionInput(sectionOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofSectionOutputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofSectionOutputToWoofSectionInputModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofSectionOutputToWoofSectionInputModel> change = this.operations
				.removeSectionOuputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSectionOutputToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param sectionIndex
	 *            {@link WoofSectionOutputModel} index.
	 */
	private void doLinkToSecurity(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofSecurityModel securityInput = this.model.getWoofSecurities().get(1);

		// Link the section output to security input
		Change<WoofSectionOutputToWoofSecurityModel> change = this.operations.linkSectionOutputToSecurity(sectionOutput,
				securityInput);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofSectionOutputToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofSectionOutputToWoofSecurityModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofSecurity();

		// Remove the link
		Change<WoofSectionOutputToWoofSecurityModel> change = this.operations.removeSectionOuputToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Security", true);
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
	 * @param sectionIndex
	 *            {@link WoofSectionModel} index.
	 */
	private void doLinkToResource(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the template output to resource
		Change<WoofSectionOutputToWoofResourceModel> change = this.operations.linkSectionOutputToResource(sectionOutput,
				resource);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofSectionOutputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofSectionOutputToWoofResourceModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofResource();

		// Remove the link
		Change<WoofSectionOutputToWoofResourceModel> change = this.operations.removeSectionOuputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Resource", true);
	}

}