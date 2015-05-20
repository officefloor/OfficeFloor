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
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

/**
 * Tests inheritance of {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class InheritanceTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure appropriately obtains the {@link WoofTemplateInheritance}.
	 */
	public void testWoofTemplateInheritance() {

		// Obtain the template inheritance information
		Map<String, WoofTemplateInheritance> superTemplates = this.operations
				.getWoofTemplateInheritances();
		assertEquals("Incorrect number of super templates", 2,
				superTemplates.size());

		// Obtain the grand parent
		WoofTemplateModel grandParent = this.model.getWoofTemplates().get(0);
		assertEquals("Should be grand parent", "GRAND_PARENT",
				grandParent.getWoofTemplateName());

		// Obtain the parent
		WoofTemplateModel parent = this.model.getWoofTemplates().get(1);
		assertEquals("Should be grand parent", "PARENT",
				parent.getWoofTemplateName());

		// Validate the grand parent inheritance
		WoofTemplateInheritance grandParentInheritance = superTemplates
				.get(grandParent.getWoofTemplateName());
		assertSame("Incorrect grand parent super template", grandParent,
				grandParentInheritance.getSuperTemplate());
		assertNotNull("Should have grand parent inheritance",
				grandParentInheritance);
		WoofTemplateModel[] grandParentHierarchy = grandParentInheritance
				.getInheritanceHierarchy();
		assertEquals("Incorrect grand parent hierarchy size", 1,
				grandParentHierarchy.length);
		assertSame("Incorrect grand parent hierarchy", grandParent,
				grandParentHierarchy[0]);
		assertEquals(
				"Incorrect grand parent template inheritance property value",
				"example/GrandParent.ofp",
				grandParentInheritance.getInheritedTemplatePathsPropertyValue());
		Set<String> grandParentOutputs = grandParentInheritance
				.getInheritedWoofTemplateOutputNames();
		assertEquals("Incorrect number of grand parent outputs", 1,
				grandParentOutputs.size());
		assertTrue("Grand parent outputs should have OUTPUT_GRAND_PARENT",
				grandParentOutputs.contains("OUTPUT_GRAND_PARENT"));

		// Validate the parent inheritance
		WoofTemplateInheritance parentInheritance = superTemplates.get(parent
				.getWoofTemplateName());
		assertSame("Incorrect parent super template", parent,
				parentInheritance.getSuperTemplate());
		assertNotNull("Should have parent inheritance", parentInheritance);
		WoofTemplateModel[] parentHierarchy = parentInheritance
				.getInheritanceHierarchy();
		assertEquals("Incorrect grand parent hierarchy size", 2,
				parentHierarchy.length);
		assertSame("Incorrect parent in hierarchy", parent, parentHierarchy[0]);
		assertSame("Incorrect grand parent in hierarchy", grandParent,
				parentHierarchy[1]);
		assertEquals("Incorrect parent template inheritance property value",
				"example/GrandParent.ofp, example/Parent.ofp",
				parentInheritance.getInheritedTemplatePathsPropertyValue());
		Set<String> parentOutputs = parentInheritance
				.getInheritedWoofTemplateOutputNames();
		assertEquals("Incorrect number of parent outputs", 2,
				parentOutputs.size());
		assertTrue("Parent outputs should have OUTPUT_PARENT",
				parentOutputs.contains("OUTPUT_PARENT"));
		assertTrue("Parent outputs should have OUTPUT_GRAND_PARENT",
				parentOutputs.contains("OUTPUT_GRAND_PARENT"));
	}

	/**
	 * Ensure able to add {@link WoofTemplateModel} with super
	 * {@link WoofTemplateModel}.
	 */
	public void testAddTemplateWithSuperTemplate() {

		// Create the section type
		SectionType section = this
				.constructSectionType(new SectionTypeConstructor() {
					@Override
					public void construct(SectionTypeContext context) {
						context.addSectionInput("renderTemplate", null);
						context.addSectionOutput(
								HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME,
								null, false);
						context.addSectionOutput("OUTPUT_GRAND_PARENT",
								Integer.class, false);
						context.addSectionOutput("OUTPUT_PARENT", null, false);
						context.addSectionOutput("OUTPUT_CHILD", null, false);
						context.addSectionOutput("NOT_INCLUDE_ESCALTION",
								IOException.class, true);
						context.addSectionObject("IGNORE_OBJECT",
								DataSource.class, null);
					}
				});

		// Obtain the parent
		WoofTemplateModel superTemplate = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect super template", "PARENT",
				superTemplate.getWoofTemplateName());

		// Add the template
		Change<WoofTemplateModel> change = this.operations.addTemplate(
				"/child", "example/Child.ofp", null, section, superTemplate,
				null, false, null, null, false, null,
				this.getWoofTemplateChangeContext());

		// Validate change
		this.assertChange(change, null, "Add Template", true);
	}

	/**
	 * Ensure able to change super {@link WoofTemplateModel} URI causing a name
	 * change and therefore child {@link WoofTemplateModel} to updates its
	 * reference to parent {@link WoofTemplateModel}.
	 */
	public void testChangeSuperTemplateUri() {

		// Obtain the grand parent template
		WoofTemplateModel grandParent = this.model.getWoofTemplates().get(0);
		assertEquals("Incorrect grand parent template", "GRAND_PARENT",
				grandParent.getWoofTemplateName());

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				grandParent, "/ancestor", this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, grandParent, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

}