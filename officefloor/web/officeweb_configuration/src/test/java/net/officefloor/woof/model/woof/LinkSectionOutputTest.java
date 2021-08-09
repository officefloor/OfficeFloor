/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

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
	 * @param sectionIndex {@link WoofSectionModel} index.
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
	 * Ensure can remove the {@link WoofSectionOutputToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofSectionOutputToWoofHttpContinuationModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofHttpContinuation();

		// Remove the link
		Change<WoofSectionOutputToWoofHttpContinuationModel> change = this.operations
				.removeSectionOutputToHttpContinuation(link);

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
	 * @param sectionIndex {@link WoofSectionModel} index.
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
		Change<WoofSectionOutputToWoofTemplateModel> change = this.operations.removeSectionOutputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSectionOutputToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param sectionIndex {@link WoofSectionOutputModel} index.
	 */
	private void doLinkToProcedure(int sectionIndex) {

		// Obtain the items to link
		WoofSectionOutputModel sectionOutput = this.model.getWoofSections().get(sectionIndex).getOutputs().get(0);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link the section output to procedure
		Change<WoofSectionOutputToWoofProcedureModel> change = this.operations
				.linkSectionOutputToProcedure(sectionOutput, procedure);

		// Validate change
		this.assertChange(change, null, "Link Section Output to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofSectionOutputToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofSectionOutputToWoofProcedureModel link = this.model.getWoofSections().get(B).getOutputs().get(0)
				.getWoofProcedure();

		// Remove the link
		Change<WoofSectionOutputToWoofProcedureModel> change = this.operations.removeSectionOutputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Procedure", true);
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
	 * @param sectionIndex {@link WoofSectionModel} index.
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
				.removeSectionOutputToSectionInput(link);

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
	 * @param sectionIndex {@link WoofSectionOutputModel} index.
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
		Change<WoofSectionOutputToWoofSecurityModel> change = this.operations.removeSectionOutputToSecurity(link);

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
	 * @param sectionIndex {@link WoofSectionModel} index.
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
		Change<WoofSectionOutputToWoofResourceModel> change = this.operations.removeSectionOutputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Section Output to Resource", true);
	}

}
