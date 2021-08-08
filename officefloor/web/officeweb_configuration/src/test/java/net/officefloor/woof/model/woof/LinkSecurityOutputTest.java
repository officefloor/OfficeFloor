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
	 * Ensure can link to {@link WoofHttpContinuationModel}.
	 */
	public void testLinkToHttpContinuation() {
		this.doLinkToHttpContinuation(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSecurityOutputToWoofHttpContinuationModel}.
	 */
	public void testLinkOverrideToHttpContinuation() {
		this.doLinkToHttpContinuation(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToHttpContinuation(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(1);

		// Link the security output to template
		Change<WoofSecurityOutputToWoofHttpContinuationModel> change = this.operations
				.linkSecurityOutputToHttpContinuation(securityOutput, httpContinuation);

		// Validate change
		this.assertChange(change, null, "Link Security Output to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofHttpContinuationModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofHttpContinuation();

		// Remove the link
		Change<WoofSecurityOutputToWoofHttpContinuationModel> change = this.operations
				.removeSecurityOutputToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to HTTP Continuation", true);
	}

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
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToTemplate(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
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
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofTemplateModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofTemplate();

		// Remove the link
		Change<WoofSecurityOutputToWoofTemplateModel> change = this.operations.removeSecurityOutputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSecurityOutputToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToProcedure(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link the security output to procedure
		Change<WoofSecurityOutputToWoofProcedureModel> change = this.operations
				.linkSecurityOutputToProcedure(securityOutput, procedure);

		// Validate change
		this.assertChange(change, null, "Link Security Output to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofProcedureModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofProcedure();

		// Remove the link
		Change<WoofSecurityOutputToWoofProcedureModel> change = this.operations.removeSecurityOutputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Procedure", true);
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
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToSectionInput(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
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
		WoofSecurityOutputToWoofSectionInputModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofSecurityOutputToWoofSectionInputModel> change = this.operations
				.removeSecurityOutputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofSecurityOutputToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToSecurity(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel securityOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
		WoofSecurityModel security = this.model.getWoofSecurities().get(1);

		// Link the security output to security
		Change<WoofSecurityOutputToWoofSecurityModel> change = this.operations
				.linkSecurityOutputToSecurity(securityOutput, security);

		// Validate change
		this.assertChange(change, null, "Link Security Output to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofSecurityOutputToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofSecurityOutputToWoofSecurityModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofSecurity();

		// Remove the link
		Change<WoofSecurityOutputToWoofSecurityModel> change = this.operations.removeSecurityOutputToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Security", true);
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
	 * @param securityIndex {@link WoofSecurityOutputModel} index.
	 */
	private void doLinkToResource(int securityIndex) {

		// Obtain the items to link
		WoofSecurityOutputModel accessOutput = this.model.getWoofSecurities().get(securityIndex).getOutputs().get(0);
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
		WoofSecurityOutputToWoofResourceModel link = this.model.getWoofSecurities().get(B).getOutputs().get(0)
				.getWoofResource();

		// Remove the link
		Change<WoofSecurityOutputToWoofResourceModel> change = this.operations.removeSecurityOutputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Security Output to Resource", true);
	}

}
