/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

		// Obtain the template to remove
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);

		// Remove the template
		Change<WoofTemplateModel> change = this.operations
				.removeTemplate(template);
		this.assertChange(change, template, "Remove template TEMPLATE", true);
	}

	/**
	 * Ensure able to remove the {@link WoofSectionModel}.
	 */
	public void testRemoveSection() {

		// Obtain the section to remove
		WoofSectionModel section = this.model.getWoofSections().get(0);

		// Remove the section
		Change<WoofSectionModel> change = this.operations
				.removeSection(section);
		this.assertChange(change, section, "Remove section SECTION", true);
	}

	/**
	 * Ensure able to remove the {@link WoofResourceModel}.
	 */
	public void testRemoveResource() {

		// Obtain the resource to remove
		WoofResourceModel resource = this.model.getWoofResources().get(0);

		// Remove the resource
		Change<WoofResourceModel> change = this.operations
				.removeResource(resource);
		this.assertChange(change, resource, "Remove resource RESOURCE", true);
	}

	/**
	 * Ensure able to remove the {@link WoofExceptionModel}.
	 */
	public void testRemoveException() {

		// Obtain the exception to remove
		WoofExceptionModel exception = this.model.getWoofExceptions().get(0);

		// Remove the exception
		Change<WoofExceptionModel> change = this.operations
				.removeException(exception);
		this.assertChange(change, exception,
				"Remove exception java.lang.Exception", true);
	}

}