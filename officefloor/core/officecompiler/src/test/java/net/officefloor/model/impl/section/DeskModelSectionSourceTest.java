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
package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.section.DeskModelSectionSource;
import net.officefloor.model.section.DeskModel;

/**
 * Tests the {@link DeskModelSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskModelSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * No specification properties required.
	 */
	public void testNoSpecification() {
		SectionLoaderUtil.validateSpecification(DeskModelSectionSource.class);
	}

	/**
	 * Ensure can source a {@link DeskModel}.
	 */
	public void testDesk() {

		// Create the expected section
		SectionDesigner designer = SectionLoaderUtil.createSectionDesigner();
		designer.addSectionInput("INPUT", Integer.class.getName());
		designer.addSectionOutput("OUTPUT", Float.class.getName(), false);
		designer.addSectionOutput("ESCALATION", Exception.class.getName(), true);
		designer.addSectionObject("OBJECT", Connection.class.getName());
		SectionFunctionNamespace work = designer.addSectionFunctionNamespace("NAMESPACE",
				MockManagedFunctionSource.class.getName());
		SectionFunction task = work.addSectionFunction("INPUT", "MANAGED_FUNCTION");
		task.getFunctionObject("PARAMETER").flagAsParameter();

		// Validates the section is as expected
		SectionLoaderUtil.validateSection(designer, DeskModelSectionSource.class, this.getClass(),
				"DeskModelSectionSourceTest.desk.xml");
	}

}