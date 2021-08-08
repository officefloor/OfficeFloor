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
public class RemoveConnectedTest extends AbstractWoofChangesTestCase {

	/**
	 * Enable able to remove the {@link WoofHttpContinuationModel}.
	 */
	public void testRemoveHttpContinuation() {

		// Obtain the HTTP Continuation
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(0);

		// Remove the HTTP Continuation
		Change<WoofHttpContinuationModel> change = this.operations.removeHttpContinuation(continuation);
		this.assertChange(change, continuation, "Remove HTTP continuation /applicationPath", true);
	}

	/**
	 * Enable able to remove the {@link WoofHttpInputModel}.
	 */
	public void testRemoveHttpInput() {

		// Obtain the HTTP Input
		WoofHttpInputModel httpInput = this.model.getWoofHttpInputs().get(0);

		// Remove the HTTP Input
		Change<WoofHttpInputModel> change = this.operations.removeHttpInput(httpInput);
		this.assertChange(change, httpInput, "Remove HTTP input POST /inputPath", true);
	}

	/**
	 * Ensure able to remove the {@link WoofTemplateModel}.
	 */
	public void testRemoveTemplate() {

		// Obtain the template to remove
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);

		// Remove the template
		Change<WoofTemplateModel> change = this.operations.removeTemplate(template,
				this.getWoofTemplateChangeContext());
		this.assertChange(change, template, "Remove template /template", true);
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
		assertEquals("Incorrect security", "SECURITY", security.getHttpSecurityName());

		// Remove the security
		Change<WoofSecurityModel> change = this.operations.removeSecurity(security);
		this.assertChange(change, security, "Remove security SECURITY", true);
	}

	/**
	 * Ensure able to remove the {@link WoofResourceModel}.
	 */
	public void testRemoveResource() {

		// Obtain the resource to remove
		WoofResourceModel resource = this.model.getWoofResources().get(0);

		// Remove the resource
		Change<WoofResourceModel> change = this.operations.removeResource(resource);
		this.assertChange(change, resource, "Remove resource /resource.html", true);
	}

	/**
	 * Ensure able to remove the {@link WoofExceptionModel} linked to a template.
	 */
	public void testRemoveException() {

		// Obtain the exception to remove
		WoofExceptionModel exception = this.model.getWoofExceptions().get(0);

		// Remove the exception
		Change<WoofExceptionModel> change = this.operations.removeException(exception);
		this.assertChange(change, exception, "Remove exception " + Exception.class.getName(), true);
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
