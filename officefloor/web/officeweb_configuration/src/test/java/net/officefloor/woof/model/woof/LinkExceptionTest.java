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
	 * Ensures can link to {@link WoofHttpContinuationModel}.
	 */
	public void testLinkToHttpContinuation() {
		this.doLinkToHttpContinuation(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofExceptionToWoofHttpContinuationModel}.
	 */
	public void testLinkOverrideToHttpContinuation() {
		this.doLinkToHttpContinuation(B);
	}

	/**
	 * Undertakes linking to a {@link WoofHttpContinuationModel}.
	 * 
	 * @param exceptionIndex
	 *            {@link WoofExceptionModel} index.
	 */
	private void doLinkToHttpContinuation(int exceptionIndex) {

		// Obtain the items to link
		WoofExceptionModel exception = this.model.getWoofExceptions().get(exceptionIndex);
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(1);

		// Link the exception to HTTP continuation
		Change<WoofExceptionToWoofHttpContinuationModel> change = this.operations
				.linkExceptionToHttpContinuation(exception, continuation);

		// Validate change
		this.assertChange(change, null, "Link Exception to HTTP Continuation", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofHttpContinuationModel}.
	 */
	public void testRemoveToHttpContinuation() {

		// Obtain the link to remove
		WoofExceptionToWoofHttpContinuationModel link = this.model.getWoofExceptions().get(B).getWoofHttpContinuation();

		// Remove the link
		Change<WoofExceptionToWoofHttpContinuationModel> change = this.operations
				.removeExceptionToHttpContinuation(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to HTTP Continuation", true);
	}

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
		WoofExceptionModel exception = this.model.getWoofExceptions().get(exceptionIndex);
		WoofTemplateModel template = this.model.getWoofTemplates().get(1);

		// Link the exception to template
		Change<WoofExceptionToWoofTemplateModel> change = this.operations.linkExceptionToTemplate(exception, template);

		// Validate change
		this.assertChange(change, null, "Link Exception to Template", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofTemplateModel}.
	 */
	public void testRemoveToTemplate() {

		// Obtain the link to remove
		WoofExceptionToWoofTemplateModel link = this.model.getWoofExceptions().get(B).getWoofTemplate();

		// Remove the link
		Change<WoofExceptionToWoofTemplateModel> change = this.operations.removeExceptionToTemplate(link);

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
		WoofExceptionModel exception = this.model.getWoofExceptions().get(exceptionIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(1).getInputs().get(0);

		// Link the exception to section input
		Change<WoofExceptionToWoofSectionInputModel> change = this.operations.linkExceptionToSectionInput(exception,
				sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Exception to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofExceptionToWoofSectionInputModel link = this.model.getWoofExceptions().get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofExceptionToWoofSectionInputModel> change = this.operations.removeExceptionToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Section Input", true);
	}

	/**
	 * Ensure can link to {@link WoofSecurityModel}.
	 */
	public void testLinkToSecurity() {
		this.doLinkToSecurity(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofExceptionToWoofSecurityModel}.
	 */
	public void testLinkOverrideToSecurity() {
		this.doLinkToSecurity(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSecurityModel}.
	 * 
	 * @param exceptionIndex
	 *            {@link WoofExceptionModel} index.
	 */
	private void doLinkToSecurity(int exceptionIndex) {

		// Obtain the items to link
		WoofExceptionModel exception = this.model.getWoofExceptions().get(exceptionIndex);
		WoofSecurityModel security = this.model.getWoofSecurities().get(1);

		// Link the exception to security
		Change<WoofExceptionToWoofSecurityModel> change = this.operations.linkExceptionToSecurity(exception, security);

		// Validate change
		this.assertChange(change, null, "Link Exception to Security", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofSecurityModel}.
	 */
	public void testRemoveToSecurity() {

		// Obtain the link to remove
		WoofExceptionToWoofSecurityModel link = this.model.getWoofExceptions().get(B).getWoofSecurity();

		// Remove the link
		Change<WoofExceptionToWoofSecurityModel> change = this.operations.removeExceptionToSecurity(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Security", true);
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
		WoofExceptionModel exception = this.model.getWoofExceptions().get(exceptionIndex);
		WoofResourceModel resource = this.model.getWoofResources().get(1);

		// Link the exception to resource
		Change<WoofExceptionToWoofResourceModel> change = this.operations.linkExceptionToResource(exception, resource);

		// Validate change
		this.assertChange(change, null, "Link Exception to Resource", true);
	}

	/**
	 * Ensure can remove the {@link WoofExceptionToWoofResourceModel}.
	 */
	public void testRemoveToResource() {

		// Obtain the link to remove
		WoofExceptionToWoofResourceModel link = this.model.getWoofExceptions().get(B).getWoofResource();

		// Remove the link
		Change<WoofExceptionToWoofResourceModel> change = this.operations.removeExceptionToResource(link);

		// Validate change
		this.assertChange(change, null, "Remove Exception to Resource", true);
	}

}