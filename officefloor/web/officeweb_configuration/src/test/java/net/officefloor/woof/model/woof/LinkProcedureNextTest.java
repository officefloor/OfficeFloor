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
 * Tests linking from the {@link WoofProcedureNextModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkProcedureNextTest extends AbstractWoofChangesTestCase {

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
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofHttpContinuationModel httpContinuation = this.model.getWoofHttpContinuations().get(1);

		// Link the procedure output to HTTP continuation
		Change<WoofProcedureNextToWoofHttpContinuationModel> change = this.operations
				.linkProcedureNextToHttpContinuation(procedureNext, httpContinuation);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofProcedureNextToWoofHttpContinuationModel link = this.model.getWoofProcedures().get(B).getNext()
				.getWoofHttpContinuation();

		// Remove the link
		Change<WoofProcedureNextToWoofHttpContinuationModel> change = this.operations
				.removeProcedureNextToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to HTTP Continuation", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToTemplate() {
		this.doLinkToTemplate(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureNextToWoofTemplateModel}.
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
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the procedure output to template
		Change<WoofProcedureNextToWoofTemplateModel> change = this.operations.linkProcedureNextToTemplate(procedureNext,
				template);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofProcedureNextToWoofTemplateModel link = this.model.getWoofProcedures().get(B).getNext().getWoofTemplate();

		// Remove the link
		Change<WoofProcedureNextToWoofTemplateModel> change = this.operations.removeProcedureNextToTemplate(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Template", true);
	}

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureNextToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureNextModel} index.
	 */
	private void doLinkToProcedure(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(1);

		// Link the procedure output to procedure
		Change<WoofProcedureNextToWoofProcedureModel> change = this.operations
				.linkProcedureNextToProcedure(procedureNext, procedure);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofProcedureNextToWoofProcedureModel link = this.model.getWoofProcedures().get(B).getNext().getWoofProcedure();

		// Remove the link
		Change<WoofProcedureNextToWoofProcedureModel> change = this.operations.removeProcedureNextToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Procedure", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureNextToWoofSectionInputModel}.
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
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link the procedure output to section input
		Change<WoofProcedureNextToWoofSectionInputModel> change = this.operations
				.linkProcedureNextToSectionInput(procedureNext, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofProcedureNextToWoofSectionInputModel link = this.model.getWoofProcedures().get(B).getNext()
				.getWoofSectionInput();

		// Remove the link
		Change<WoofProcedureNextToWoofSectionInputModel> change = this.operations
				.removeProcedureNextToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofProcedureNextToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param procedureIndex {@link WoofProcedureNextModel} index.
	 */
	private void doLinkToSecurity(int procedureIndex) {

		// Obtain the items to link
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofSecurityModel securityInput = this.model.getWoofSecurities().get(1);

		// Link the procedure output to security input
		Change<WoofProcedureNextToWoofSecurityModel> change = this.operations.linkProcedureNextToSecurity(procedureNext,
				securityInput);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofProcedureNextToWoofSecurityModel link = this.model.getWoofProcedures().get(B).getNext().getWoofSecurity();

		// Remove the link
		Change<WoofProcedureNextToWoofSecurityModel> change = this.operations.removeProcedureNextToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Security", true);
	}

	/**
	 * Ensure can link to {@link WoofResourceModel}.
	 */
	public void testLinkToResource() {
		this.doLinkToResource(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofTemplateNextToWoofResourceModel}.
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
		WoofProcedureNextModel procedureNext = this.model.getWoofProcedures().get(procedureIndex).getNext();
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the template output to resource
		Change<WoofProcedureNextToWoofResourceModel> change = this.operations.linkProcedureNextToResource(procedureNext,
				resource);

		// Validate change
		this.assertChange(change, null, "Link Procedure Next to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofProcedureNextToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofProcedureNextToWoofResourceModel link = this.model.getWoofProcedures().get(B).getNext().getWoofResource();

		// Remove the link
		Change<WoofProcedureNextToWoofResourceModel> change = this.operations.removeProcedureNextToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Procedure Next to Resource", true);
	}

}
