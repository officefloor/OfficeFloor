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
package net.officefloor.compile.impl.section;

import java.io.IOException;
import java.sql.Connection;

import junit.framework.TestCase;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Class for {@link ClassSectionSource} that enables validating loading a
 * {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadSection {

	/**
	 * Asserts the {@link SectionType} is valid.
	 * 
	 * @param sectionType
	 *            {@link SectionType}.
	 */
	public static void assertSectionType(SectionType sectionType) {

		// Validate the inputs
		TestCase.assertEquals("Incorrect number of inputs", 2, sectionType
				.getSectionInputTypes().length);
		SectionInputType assertSectionType = sectionType.getSectionInputTypes()[0];
		TestCase.assertEquals("Incorrect section input name",
				"assertSectionType", assertSectionType.getSectionInputName());
		TestCase.assertNull("Incorrect parameter type", assertSectionType
				.getParameterType());
		SectionInputType doInput = sectionType.getSectionInputTypes()[1];
		TestCase.assertEquals("Incorrect section input name", "doInput",
				doInput.getSectionInputName());
		TestCase.assertEquals("Incorrect parameter type", String.class
				.getName(), doInput.getParameterType());

		// Validate the outputs
		TestCase.assertEquals("Incorrect number of outputs", 2, sectionType
				.getSectionOutputTypes().length);
		SectionOutputType doOutput = sectionType.getSectionOutputTypes()[0];
		TestCase.assertEquals("Incorrect section output name", "doOutput",
				doOutput.getSectionOutputName());
		TestCase.assertEquals("Incorrect argument type", Integer.class
				.getName(), doOutput.getArgumentType());
		TestCase.assertEquals("Incorrect is escalation", false, doOutput
				.isEscalationOnly());
		SectionOutputType escalation = sectionType.getSectionOutputTypes()[1];
		TestCase.assertEquals("Incorrect section output name",
				"java.io.IOException", escalation.getSectionOutputName());
		TestCase.assertEquals("Incorrect argument type", IOException.class
				.getName(), escalation.getArgumentType());
		TestCase.assertEquals("Incorrect is escalation", true, escalation
				.isEscalationOnly());

		// Validate the objects
		TestCase.assertEquals("Incorrect number of objects", 2, sectionType
				.getSectionObjectTypes().length);
		SectionObjectType connection = sectionType.getSectionObjectTypes()[0];
		TestCase.assertEquals("Incorrect section object name",
				"java.sql.Connection", connection.getSectionObjectName());
		TestCase.assertEquals("Incorrect object type", Connection.class
				.getName(), connection.getObjectType());
		SectionObjectType sectionTypeObject = sectionType
				.getSectionObjectTypes()[1];
		TestCase.assertEquals("Incorrect section object name",
				SectionType.class.getName(), sectionTypeObject
						.getSectionObjectName());
		TestCase.assertEquals("Incorrect object type", SectionType.class
				.getName(), sectionTypeObject.getObjectType());
	}

	/**
	 * Input to the section.
	 * 
	 * @param parameter
	 *            Parameter to the {@link SectionInput}.
	 * @param connection
	 *            Dependency of the section.
	 * @return Parameter for the {@link SectionOutput}.
	 * @throws IOException
	 *             Escalation.
	 */
	@Next("doOutput")
	public Integer doInput(@Parameter String parameter, Connection connection)
			throws IOException {
		return null;
	}

}