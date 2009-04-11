/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.section;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SubSection;

/**
 * Tests loading the {@link OfficeSection}.
 * 
 * @author Daniel
 */
public class LoadOfficeSectionTest extends AbstractOfficeSectionTestCase {

	/**
	 * Ensure can load an empty {@link OfficeSection}.
	 */
	public void testLoadEmptySection() {

		// Load the empty office section
		OfficeSection section = this.loadOfficeSection(new SectionMaker() {
			@Override
			public void make(SectionMakerContext context) {
				// Leave empty
			}
		});

		// Ensure empty
		assertEquals("Should be no sub section", 0,
				section.getSubSections().length);
		assertEquals("Should be no tasks", 0, section.getTasks().length);
	}

	/**
	 * Ensure can load a {@link SubSection}.
	 */
	public void testLoadSubSection() {

		// Load the office section with a sub section
		OfficeSection section = this.loadOfficeSection(new SectionMaker() {
			@Override
			public void make(SectionMakerContext context) {
				context.addSubSection("SUB_SECTION", null);
			}
		});

		// Validate results
		assertEquals("Should have a sub section", 1,
				section.getSubSections().length);
		OfficeSection subSection = section.getSubSections()[0];
		assertEquals("Incorrect sub section", "SUB_SECTION", subSection
				.getSectionName());
		assertEquals("Should be no sub section tasks", 0,
				subSection.getTasks().length);
		assertEquals("Should be no tasks", 0, section.getTasks().length);
	}

	/**
	 * Ensure can load a sub {@link SubSection}. To ensure recursive loading of
	 * the {@link SubSection} instances.
	 */
	public void testLoadSubSubSection() {

		// Load the office section with a sub sub section
		OfficeSection section = this.loadOfficeSection(new SectionMaker() {
			@Override
			public void make(SectionMakerContext context) {
				context.addSubSection("SUB_SECTION", new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addSubSection("SUB_SUB_SECTION", null);
					}
				});
			}
		});

		// Validate the results
		assertEquals("Should have a sub section", 1,
				section.getSubSections().length);
		assertEquals("Should be no tasks on section", 0,
				section.getTasks().length);
		OfficeSection subSection = section.getSubSections()[0];
		assertEquals("Should have a sub sub section", 1, subSection
				.getSubSections().length);
		assertEquals("Should be no tasks on sub section", 0, subSection
				.getTasks().length);
		OfficeSection subSubSection = subSection.getSubSections()[0];
		assertEquals("Incorrect sub sub section", "SUB_SUB_SECTION",
				subSubSection.getSectionName());
		assertEquals("Should be no tasks on sub sub section", 0, subSubSection
				.getTasks().length);
	}

	/**
	 * Ensure can load a {@link SectionTask}.
	 */
	public void testLoadSectionTask() {

		// Load the office section with a section task
		OfficeSection section = this.loadOfficeSection(new SectionMaker() {
			@Override
			public void make(SectionMakerContext context) {
				context.addTask("WORK", "TASK", null);
			}
		});

		// Validate results
		assertEquals("Should be no sub sections", 0,
				section.getSubSections().length);
		assertEquals("Should have a single task", 1, section.getTasks().length);
		OfficeTask task = section.getTasks()[0];
		assertEquals("Incorrect task name", "TASK", task.getTaskName());
	}

}