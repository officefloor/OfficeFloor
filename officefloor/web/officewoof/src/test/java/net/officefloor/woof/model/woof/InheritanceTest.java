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

import java.io.IOException;

import javax.sql.DataSource;

import net.officefloor.model.change.Change;

/**
 * Tests inheritance of {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class InheritanceTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to add {@link WoofTemplateModel} with super
	 * {@link WoofTemplateModel}.
	 */
	public void testAddTemplateWithSuperTemplate() {

		// Create the section type
		this.constructSectionType(new SectionTypeConstructor() {
			@Override
			public void construct(SectionTypeContext context) {
				context.addSectionInput("renderTemplate", null);
				context.addSectionOutput("OUTPUT_GRAND_PARENT", Integer.class, false);
				context.addSectionOutput("OUTPUT_PARENT", null, false);
				context.addSectionOutput("OUTPUT_CHILD", null, false);
				context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
				context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
			}
		});

		// Obtain the orphan template
		WoofTemplateModel orphanTemplate = this.model.getWoofTemplates().get(2);
		assertEquals("Incorrect orphan template", "ORPHAN", orphanTemplate.getApplicationPath());

		// Obtain the parent
		WoofTemplateModel superTemplate = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect super template", "PARENT", superTemplate.getApplicationPath());

		// Link super template
		Change<WoofTemplateToSuperWoofTemplateModel> change = this.operations
				.linkTemplateToSuperTemplate(orphanTemplate, superTemplate);

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to change super {@link WoofTemplateModel} URI causing a name
	 * change and therefore child {@link WoofTemplateModel} to updates its
	 * reference to parent {@link WoofTemplateModel}.
	 */
	public void testChangeSuperTemplateApplicationPath() {

		// Obtain the grand parent template
		WoofTemplateModel grandParent = this.model.getWoofTemplates().get(0);
		assertEquals("Incorrect grand parent template", "/grandparent", grandParent.getApplicationPath());

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(grandParent, "/ancestor",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, grandParent, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

}