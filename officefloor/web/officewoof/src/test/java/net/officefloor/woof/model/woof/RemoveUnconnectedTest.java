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
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Tests removing from a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveUnconnectedTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to remove the {@link WoofApplicationPathModel}.
	 */
	public void testRemoveApplicationPath() {

		// Obtain the application path to remove
		WoofApplicationPathModel path = this.model.getWoofApplicationPaths().get(0);

		// Remove the application path
		Change<WoofApplicationPathModel> change = this.operations.removeApplicationPath(path);
		this.assertChange(change, path, "Remove application path /path", true);
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
		this.assertChange(change, security, "Remove security net.example.HttpSecuritySource", true);
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