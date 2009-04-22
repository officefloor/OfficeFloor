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
package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.section.SectionModel;

/**
 * Tests the {@link SectionModelSectionSource}.
 * 
 * @author Daniel
 */
public class SectionModelSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * No specification properties required.
	 */
	public void testNoSpecification() {
		SectionLoaderUtil
				.validateSpecification(SectionModelSectionSource.class);
	}

	/**
	 * Ensure can source a {@link SectionModel}.
	 */
	public void testSection() {

		// Create the expected section
		SectionDesigner designer = SectionLoaderUtil
				.createSectionDesigner(SectionModelSectionSource.class);
		designer.addSectionInput("INPUT", Integer.class.getName());
		designer.addSectionOutput("OUTPUT", Float.class.getName(), false);
		designer
				.addSectionOutput("ESCALATION", Exception.class.getName(), true);
		designer.addSectionObject("OBJECT", Connection.class.getName());

		// Validate the section is as expected
		SectionLoaderUtil.validateSection(designer,
				SectionModelSectionSource.class, this,
				"SectionModelSectionSourceTest.section.xml");
	}

}