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
 * Tests linking from the {@link WoofHttpContinuationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkHttpContinuationTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of {@link WoofHttpContinuationModel} A.
	 */
	private static final int A = 0;

	/**
	 * Index of {@link WoofHttpContinuationModel} B.
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
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToHttpContinuation(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofHttpContinuationModel httpRedirect = this.model.getWoofHttpContinuations().get(1);

		// Link to HTTP continuation
		Change<WoofHttpContinuationToWoofHttpContinuationModel> change = this.operations
				.linkHttpContinuationToHttpContinuation(httpContinuation, httpRedirect);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the
	 * {@link WoofHttpContinuationToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofHttpContinuationModel link = this.model.getWoofHttpContinuations().get(B)
				.getWoofRedirect();

		// Remove the link
		Change<WoofHttpContinuationToWoofHttpContinuationModel> change = this.operations
				.removeHttpContinuationToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to HTTP Continuation", true);
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
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToTemplate(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link to template
		Change<WoofHttpContinuationToWoofTemplateModel> change = this.operations
				.linkHttpContinuationToTemplate(httpContinuation, template);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpContinuationToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofTemplateModel link = this.model.getWoofHttpContinuations().get(B).getWoofTemplate();

		// Remove the link
		Change<WoofHttpContinuationToWoofTemplateModel> change = this.operations.removeHttpContinuationToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to Template", true);
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
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToSectionInput(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link to section input
		Change<WoofHttpContinuationToWoofSectionInputModel> change = this.operations
				.linkHttpContinuationToSectionInput(httpContinuation, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpContinuationToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofSectionInputModel link = this.model.getWoofHttpContinuations().get(B)
				.getWoofSectionInput();

		// Remove the link
		Change<WoofHttpContinuationToWoofSectionInputModel> change = this.operations
				.removeHttpContinuationToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpContinuationToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToProcedure(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link to procedure
		Change<WoofHttpContinuationToWoofProcedureModel> change = this.operations
				.linkHttpContinuationToProcedure(httpContinuation, procedure);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofProcedureModel link = this.model.getWoofHttpContinuations().get(B).getWoofProcedure();

		// Remove the link
		Change<WoofHttpContinuationToWoofProcedureModel> change = this.operations
				.removeHttpContinuationToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to Procedure", true);
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
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToSecurity(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofSecurityModel security = this.model.getWoofSecurities().get(1);

		// Link to security
		Change<WoofHttpContinuationToWoofSecurityModel> change = this.operations
				.linkHttpContinuationToSecurity(httpContinuation, security);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpInputToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofSecurityModel link = this.model.getWoofHttpContinuations().get(B).getWoofSecurity();

		// Remove the link
		Change<WoofHttpContinuationToWoofSecurityModel> change = this.operations.removeHttpContinuationToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to Security", true);
	}

	/**
	 * Ensure can link to {@link WoofResourceModel}.
	 */
	public void testLinkToResource() {
		this.doLinkToResource(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofHttpContinuationToWoofResourceModel}.
	 */
	public void testLinkOverrideToResource() {
		this.doLinkToResource(B);
	}

	/**
	 * Undertakes linking to a {@link WoofResourceModel}.
	 * 
	 * @param index {@link WoofHttpContinuationModel} index.
	 */
	private void doLinkToResource(int index) {

		// Obtain the items to link
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(index);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link to resource
		Change<WoofHttpContinuationToWoofResourceModel> change = this.operations
				.linkHttpContinuationToResource(httpContinuation, resource);

		// Validate change
		this.assertChange(change, null, "Link HTTP Continuation to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofHttpContinuationToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofHttpContinuationToWoofResourceModel link = this.model.getWoofHttpContinuations().get(B).getWoofResource();

		// Remove the link
		Change<WoofHttpContinuationToWoofResourceModel> change = this.operations.removeHttpContinuationToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove HTTP Continuation to Resource", true);
	}

}
