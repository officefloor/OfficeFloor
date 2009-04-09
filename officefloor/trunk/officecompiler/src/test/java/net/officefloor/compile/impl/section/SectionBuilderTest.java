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

import java.sql.Connection;

import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SectionBuilder}.
 * 
 * @author Daniel
 */
public class SectionBuilderTest extends OfficeFrameTestCase {

	/**
	 * {@link SectionBuilder} to be tested.
	 */
	private SectionBuilder builder = new SectionBuilderImpl();

	/**
	 * Tests obtaining a {@link SectionInput}.
	 */
	public void testSectionInput() {
		SectionInput input = this.builder.addInput("INPUT", String.class
				.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input
				.getSectionInputName());
		assertEquals("Should obtain same input", input, this.builder.addInput(
				"INPUT", String.class.getName()));
		assertNotSame("Should obtain another input", input, this.builder
				.addInput("ANOTHER", Integer.class.getName()));
	}

	/**
	 * Tests obtaining a {@link SectionOutput}.
	 */
	public void testSectionOutput() {
		SectionOutput output = this.builder.addOutput("OUTPUT", Double.class
				.getName(), false);
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output
				.getSectionOutputName());
		assertEquals("Should obtain same output", output, this.builder
				.addOutput("OUTPUT", Double.class.getName(), false));
		assertNotSame("Should obtain another output", output, this.builder
				.addOutput("ANOTHER", Exception.class.getName(), true));
	}

	/**
	 * Tests obtaining a {@link SectionObject}.
	 */
	public void testSectionObject() {
		SectionObject object = this.builder.addObject("OBJECT",
				Connection.class.getName());
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object
				.getSectionObjectName());
		assertEquals("Should obtain same object", object, this.builder
				.addObject("OBJECT", Connection.class.getName()));
		assertNotSame("Should obtain another object", object, this.builder
				.addObject("ANOTHER", Object.class.getName()));
	}

	/**
	 * Ensure can add a {@link SubSection}.
	 */
	public void testAddSubSection() {
		// SubSection subSection = this.builder.addSubSection("SUB_SECTION",
		// "net.example.ExampleSectionSource", "LOCATION");
	}

	/**
	 * Mock {@link SectionSource} for testing.
	 */
	public static class MockSectionSource implements SectionSource {

		/*
		 * ==================== SectionSource =============================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceSection(SectionBuilder sectionBuilder,
				SectionSourceContext context) throws Exception {
			fail("TODO implement obtaining section");
		}
	}
}