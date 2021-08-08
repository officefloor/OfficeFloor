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
 * Tests linking from the {@link WoofHttpInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkHttpInputTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of {@link WoofHttpInputModel} A.
	 */
	private static final int A = 0;

	/**
	 * Index of {@link WoofHttpInputModel} B.
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
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToHttpContinuation(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(1);

		// Link to template
		Change<WoofHttpInputToWoofHttpContinuationModel> change = this.operations
				.linkHttpInputToHttpContinuation(httpInput, httpContinuation);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofHttpInputToWoofHttpContinuationModel link = this.model.getWoofHttpInputs().get(B).getWoofHttpContinuation();

		// Remove the link
		Change<WoofHttpInputToWoofHttpContinuationModel> change = this.operations
				.removeHttpInputToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to HTTP Continuation", true);
	}

	/**
	 * Ensure can link to {@link WoofTemplateModel}.
	 */
	public void testLinkToTemplate() {
		this.doLinkToTemplate(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpInputToWoofTemplateModel}.
	 */
	public void testLinkOverrideToTemplate() {
		this.doLinkToTemplate(B);
	}

	/**
	 * Undertakes linking to a {@link WoofTemplateModel}.
	 * 
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToTemplate(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the security output to template
		Change<WoofHttpInputToWoofTemplateModel> change = this.operations.linkHttpInputToTemplate(httpInput, template);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofHttpInputToWoofTemplateModel link = this.model.getWoofHttpInputs().get(B).getWoofTemplate();

		// Remove the link
		Change<WoofHttpInputToWoofTemplateModel> change = this.operations.removeHttpInputToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpInputToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToSectionInput(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link to section input
		Change<WoofHttpInputToWoofSectionInputModel> change = this.operations.linkHttpInputToSectionInput(httpInput,
				sectionInput);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofHttpInputToWoofSectionInputModel link = this.model.getWoofHttpInputs().get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofHttpInputToWoofSectionInputModel> change = this.operations.removeHttpInputToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpInputToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToProcedure(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link to procedure
		Change<WoofHttpInputToWoofProcedureModel> change = this.operations.linkHttpInputToProcedure(httpInput,
				procedure);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofHttpInputToWoofProcedureModel link = this.model.getWoofHttpInputs().get(B).getWoofProcedure();

		// Remove the link
		Change<WoofHttpInputToWoofProcedureModel> change = this.operations.removeHttpInputToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to Procedure", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpInputToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToSecurity(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofSecurityModel security = this.model.getWoofSecurities().get(1);

		// Link to security
		Change<WoofHttpInputToWoofSecurityModel> change = this.operations.linkHttpInputToSecurity(httpInput, security);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofHttpInputToWoofSecurityModel link = this.model.getWoofHttpInputs().get(B).getWoofSecurity();

		// Remove the link
		Change<WoofHttpInputToWoofSecurityModel> change = this.operations.removeHttpInputToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to Security", true);
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
	 * @param index {@link WoofHttpInputModel} index.
	 */
	private void doLinkToResource(int index) {

		// Obtain the items to link
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(index);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link to resource
		Change<WoofHttpInputToWoofResourceModel> change = this.operations.linkHttpInputToResource(httpInput, resource);

		// Validate change
		this.assertChange(change, null, "Link HTTP Input to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofHttpInputToWoofResourceModel link = this.model.getWoofHttpInputs().get(B).getWoofResource();

		// Remove the link
		Change<WoofHttpInputToWoofResourceModel> change = this.operations.removeHttpInputToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Input to Resource", true);
	}

}
