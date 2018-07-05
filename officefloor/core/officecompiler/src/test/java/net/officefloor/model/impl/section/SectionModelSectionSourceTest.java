/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.section.SectionModel;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link SectionModelSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionModelSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * No specification properties required.
	 */
	public void testNoSpecification() {
		SectionLoaderUtil.validateSpecification(SectionModelSectionSource.class);
	}

	/**
	 * Ensure can source a {@link SectionModel}.
	 */
	public void testSection() {

		// Create the expected section
		SectionDesigner designer = SectionLoaderUtil.createSectionDesigner();
		designer.addSectionInput("sectionInput", Integer.class.getName());
		designer.addSectionInput("FUNCTION_INPUT", Long.class.getName());
		designer.addSectionOutput("OUTPUT", Float.class.getName(), false);
		designer.addSectionOutput("ESCALATION", Exception.class.getName(), true);
		designer.addSectionObject("OBJECT", Connection.class.getName());
		designer.addSubSection("SUB_SECTION", ClassSectionSource.class.getName(), MockSection.class.getName());
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
				MockManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION_INPUT", "MANAGED_FUNCTION");
		function.getFunctionObject("PARAMETER").flagAsParameter();

		// Validate the section is as expected
		SectionLoaderUtil.validateSection(designer, SectionModelSectionSource.class, this.getClass(),
				"SectionModelSectionSourceTest.section.xml");
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		public Integer sectionInput() {
			return null;
		}
	}

}