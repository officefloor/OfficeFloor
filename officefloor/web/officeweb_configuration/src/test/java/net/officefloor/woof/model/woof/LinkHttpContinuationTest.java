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
	 * @param index
	 *            {@link WoofHttpContinuationModel} index.
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
	 * @param index
	 *            {@link WoofHttpContinuationModel} index.
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
	 * @param index
	 *            {@link WoofHttpContinuationModel} index.
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
	 * Ensure can remove the
	 * {@link WoofHttpContinuationToWoofSectionInputModel}.
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
	 * @param index
	 *            {@link WoofHttpContinuationModel} index.
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
	 * @param index
	 *            {@link WoofHttpContinuationModel} index.
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