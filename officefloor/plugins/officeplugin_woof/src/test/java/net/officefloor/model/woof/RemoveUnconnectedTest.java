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
package net.officefloor.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests removing from a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveUnconnectedTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to remove the {@link WoofTemplateModel}.
	 */
	public void testRemoveTemplate() {

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "template",
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
		this.assertChange(change, template, "Remove template TEMPLATE", true);

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
	 * Ensure able to remove the {@link WoofAccessModel}.
	 */
	public void testRemoveAccess() {

		// Obtain the access to remove
		WoofAccessModel access = this.model.getWoofAccesses().get(0);

		// Remove the access
		Change<WoofAccessModel> change = this.operations.removeAccess(access);
		this.assertChange(change, access, "Remove access net.example.HttpSecuritySource", true);
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
		this.assertChange(change, resource, "Remove resource RESOURCE", true);
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