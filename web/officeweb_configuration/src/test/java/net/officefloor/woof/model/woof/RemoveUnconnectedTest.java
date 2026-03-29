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
 * Tests removing from a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveUnconnectedTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to remove the {@link WoofHttpContinuationModel}.
	 */
	public void testRemoveHttpContinuation() {

		// Obtain the HTTP Continuation to remove
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(0);

		// Remove the application path
		Change<WoofHttpContinuationModel> change = this.operations.removeHttpContinuation(continuation);
		this.assertChange(change, continuation, "Remove HTTP continuation /path", true);
	}

	/**
	 * Ensure able to remove the {@link WoofHttpInputModel}.
	 */
	public void testRemoveHttpInput() {

		// Obtain the HTTP Input to remove
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(0);

		// Remove the HTTP Input
		Change<WoofHttpInputModel> change = this.operations.removeHttpInput(httpInput);
		this.assertChange(change, httpInput, "Remove HTTP input POST /input", true);
	}

	/**
	 * Ensure able to remove the {@link WoofTemplateModel}.
	 */
	public void testRemoveTemplate() {

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "/template",
				new String[] { "ONE", "A", "TWO", "B" }, null, null, this.getWoofTemplateChangeContext());

		// Record extension change
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Obtain the template to remove
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);

		// Test
		this.replayMockObjects();

		// Remove the template
		Change<WoofTemplateModel> change = this.operations.removeTemplate(template,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, template, "Remove template /template", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to remove the {@link WoofProcedureModel}.
	 */
	public void testRemoveProcedure() {

		// Obtain the procedure to remove
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(0);

		// Remove the procedure
		Change<WoofProcedureModel> change = this.operations.removeProcedure(procedure);
		this.assertChange(change, procedure, "Remove procedure PROCEDURE", true);
	}

	/**
	 * Ensure able to remove the {@link WoofSectionModel}.
	 */
	public void testRemoveSection() {

		// Obtain the section to remove
		WoofSectionModel section = this.model.getWoofSections().get(0);

		// Remove the section
		Change<WoofSectionModel> change = this.operations.removeSection(section);
		this.assertChange(change, section, "Remove section SECTION", true);
	}

	/**
	 * Ensure able to remove the {@link WoofSecurityModel}.
	 */
	public void testRemoveSecurity() {

		// Obtain the security to remove
		WoofSecurityModel security = this.model.getWoofSecurities().get(0);

		// Remove the security
		Change<WoofSecurityModel> change = this.operations.removeSecurity(security);
		this.assertChange(change, security, "Remove security SECURITY", true);
	}

	/**
	 * Ensure able to remove the {@link WoofGovernanceModel}.
	 */
	public void testRemoveGovernance() {

		// Obtain the governance to remove
		WoofGovernanceModel governance = this.model.getWoofGovernances().get(0);

		// Remove the governance
		Change<WoofGovernanceModel> change = this.operations.removeGovernance(governance);
		this.assertChange(change, governance, "Remove governance GOVERNANCE", true);
	}

	/**
	 * Ensure able to remove the {@link WoofResourceModel}.
	 */
	public void testRemoveResource() {

		// Obtain the resource to remove
		WoofResourceModel resource = this.model.getWoofResources().get(0);

		// Remove the resource
		Change<WoofResourceModel> change = this.operations.removeResource(resource);
		this.assertChange(change, resource, "Remove resource Example.html", true);
	}

	/**
	 * Ensure able to remove the {@link WoofExceptionModel}.
	 */
	public void testRemoveException() {

		// Obtain the exception to remove
		WoofExceptionModel exception = this.model.getWoofExceptions().get(0);

		// Remove the exception
		Change<WoofExceptionModel> change = this.operations.removeException(exception);
		this.assertChange(change, exception, "Remove exception java.lang.Exception", true);
	}

	/**
	 * Ensure able to remove the {@link WoofStartModel}.
	 */
	public void testRemoveStart() {

		// Obtain the start to remove
		WoofStartModel start = this.model.getWoofStarts().get(0);

		// Remove the start
		Change<WoofStartModel> change = this.operations.removeStart(start);
		this.assertChange(change, start, "Remove start", true);
	}

}
