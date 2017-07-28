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

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.model.change.Change;

/**
 * Tests removing from a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveConnectedTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to remove the {@link WoofTemplateModel}.
	 */
	public void testRemoveTemplate() {

		// Obtain the template to remove
		WoofTemplateModel template = this.model.getWoofTemplates().get(0);

		// Remove the template
		Change<WoofTemplateModel> change = this.operations.removeTemplate(template,
				this.getWoofTemplateChangeContext());
		this.assertChange(change, template, "Remove template TEMPLATE", true);
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
	 * Ensure able to remove the {@link WoofExceptionModel} linked to a
	 * template.
	 */
	public void testRemoveExceptionLinkedToTemplate() {
		this.doRemoveExceptionTest(0, IOException.class);
	}

	/**
	 * Ensure able to remove the {@link WoofExceptionModel} linked to a section.
	 */
	public void testRemoveExceptionLinkedToSection() {
		this.doRemoveExceptionTest(1, RuntimeException.class);
	}

	/**
	 * Ensure able to remove the {@link WoofExceptionModel} linked to a
	 * resource.
	 */
	public void testRemoveExceptionLinkedToResource() {
		this.doRemoveExceptionTest(2, SQLException.class);
	}

	/**
	 * Does the remove {@link WoofExceptionModel} test.
	 * 
	 * @param index
	 *            Index of the {@link WoofExceptionModel} to remove.
	 * @param exceptionClass
	 *            Class of the {@link Exception}.
	 */
	private void doRemoveExceptionTest(int index, Class<? extends Throwable> exceptionClass) {

		// Obtain the exception to remove
		WoofExceptionModel exception = this.model.getWoofExceptions().get(index);

		// Remove the exception
		Change<WoofExceptionModel> change = this.operations.removeException(exception);
		this.assertChange(change, exception, "Remove exception " + exceptionClass.getName(), true);
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