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
 * Tests linking from the {@link WoofProcedureOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkProcedureOutputTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of procedure A.
	 */
	private static final int A = 0;

	/**
	 * Index of procedure B.
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
	 * {@link WoofProcedureOutputToWoofHttpContinuationModel}.
	 */
	public void testLinkOverrideToHttpContinuation() {
		this.doLinkToHttpContinuation(B);
	}

	/**
	 * Undertakes linking to a {@link WoofHttpContinuationModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureModel} index.
	 */
	private void doLinkToHttpContinuation(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(1);

		// Link the procedure output to HTTP continuation
		Change<WoofProcedureOutputToWoofHttpContinuationModel> change = this.operations
				.linkProcedureOutputToHttpContinuation(procedureOutput, httpContinuation);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofHttpContinuationModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofHttpContinuation();

		// Remove the link
		Change<WoofProcedureOutputToWoofHttpContinuationModel> change = this.operations
				.removeProcedureOutputToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to HTTP Continuation", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToTemplate() {
		this.doLinkToTemplate(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureOutputToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureModel} index.
	 */
	private void doLinkToTemplate(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the procedure output to template
		Change<WoofProcedureOutputToWoofTemplateModel> change = this.operations
				.linkProcedureOutputToTemplate(procedureOutput, template);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofTemplateModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofTemplate();

		// Remove the link
		Change<WoofProcedureOutputToWoofTemplateModel> change = this.operations.removeProcedureOutputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureOutputToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureOutputModel} index.
	 */
	private void doLinkToProcedure(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link the procedure output to procedure
		Change<WoofProcedureOutputToWoofProcedureModel> change = this.operations
				.linkProcedureOutputToProcedure(procedureOutput, procedure);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofProcedureModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofProcedure();

		// Remove the link
		Change<WoofProcedureOutputToWoofProcedureModel> change = this.operations.removeProcedureOutputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Procedure", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureOutputToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureModel} index.
	 */
	private void doLinkToSectionInput(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link the procedure output to section input
		Change<WoofProcedureOutputToWoofSectionInputModel> change = this.operations
				.linkProcedureOutputToSectionInput(procedureOutput, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofSectionInputModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofProcedureOutputToWoofSectionInputModel> change = this.operations
				.removeProcedureOutputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureOutputToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureOutputModel} index.
	 */
	private void doLinkToSecurity(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofSecurityModel securityInput = this.model.getWoofSecurities().get(1);

		// Link the procedure output to security input
		Change<WoofProcedureOutputToWoofSecurityModel> change = this.operations
				.linkProcedureOutputToSecurity(procedureOutput, securityInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofSecurityModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofSecurity();

		// Remove the link
		Change<WoofProcedureOutputToWoofSecurityModel> change = this.operations.removeProcedureOutputToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Security", true);
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
	 * @param procedureIndex {@link WoofProcedureModel} index.
	 */
	private void doLinkToResource(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureOutputModel procedureOutput = this.model.getWoofProcedures().get(procedureIndex).getOutputs()
				.get(0);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the template output to resource
		Change<WoofProcedureOutputToWoofResourceModel> change = this.operations
				.linkProcedureOutputToResource(procedureOutput, resource);

		// Validate change
		this.assertChange(change, null, "Link Procedure Output to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureOutputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofProcedureOutputToWoofResourceModel link = this.model.getWoofProcedures().get(B).getOutputs().get(0)
				.getWoofResource();

		// Remove the link
		Change<WoofProcedureOutputToWoofResourceModel> change = this.operations.removeProcedureOutputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Output to Resource", true);
	}

}
