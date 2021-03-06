/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.office;

import java.sql.Connection;

import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Mock {@link OfficeSource} to test loading the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadOfficeSource extends AbstractOfficeSource {

	/**
	 * Property name that is required to be configured.
	 */
	public static final String PROPERTY_REQUIRED = "required.property";

	/**
	 * Asserts the {@link OfficeType} is correct.
	 * 
	 * @param officeType
	 *            {@link OfficeType}.
	 */
	public static void assertOfficeType(OfficeType officeType) {

		// Ensure correct section inputs
		TestCase.assertEquals("Incorrect number of inputs", 1, officeType.getOfficeSectionInputTypes().length);
		OfficeAvailableSectionInputType sectionInput = officeType.getOfficeSectionInputTypes()[0];
		TestCase.assertEquals("Incorrect section name", "section", sectionInput.getOfficeSectionName());
		TestCase.assertEquals("Incorrect input name", "input", sectionInput.getOfficeSectionInputName());
		TestCase.assertEquals("Incorrect input parameter type", String.class.getName(),
				sectionInput.getParameterType());

		// Ensure correct objects
		TestCase.assertEquals("Incorrect number of objects", 1, officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType objectType = officeType.getOfficeManagedObjectTypes()[0];
		TestCase.assertEquals("Incorrect object name", "object", objectType.getOfficeManagedObjectName());
		TestCase.assertEquals("Incorrect object type", Connection.class.getName(), objectType.getObjectType());
		TestCase.assertEquals("Incorrect number of extension interfaces", 1,
				objectType.getExtensionInterfaces().length);
		TestCase.assertEquals("Incorrect extension interface", XAResource.class.getName(),
				objectType.getExtensionInterfaces()[0]);

		// Ensure correct team
		TestCase.assertEquals("Incorrect number of teams", 1, officeType.getOfficeTeamTypes().length);
		TestCase.assertEquals("Incorrect team name", "team", officeType.getOfficeTeamTypes()[0].getOfficeTeamName());

		// Ensure correct inputs
		TestCase.assertEquals("Incorrect number of inputs", 1, officeType.getOfficeInputTypes().length);
		OfficeInputType input = officeType.getOfficeInputTypes()[0];
		TestCase.assertEquals("Incorrect input name", "INPUT", input.getOfficeInputName());
		TestCase.assertEquals("Incorrect input parameter type", Integer.class.getName(), input.getParameterType());

		// Ensure correct outputs
		TestCase.assertEquals("Incorrect number of outputs", 1, officeType.getOfficeOutputTypes().length);
		OfficeOutputType output = officeType.getOfficeOutputTypes()[0];
		TestCase.assertEquals("Incorrect output name", "OUTPUT", output.getOfficeOutputName());
		TestCase.assertEquals("Incorrect output argument type", Long.class.getName(), output.getArgumentType());
	}

	/*
	 * =================== OfficeSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect, OfficeSourceContext context) throws Exception {

		// Obtain the required property
		context.getProperty(PROPERTY_REQUIRED);

		// Add the input
		architect.addOfficeSection("section", ClassSectionSource.class.getName(), MockSection.class.getName());

		// Add the object
		OfficeObject object = architect.addOfficeObject("object", Connection.class.getName());
		OfficeAdministration admin = architect.addOfficeAdministration("admin",
				ClassAdministrationSource.class.getName());
		admin.addProperty(ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockAdministrator.class.getName());
		admin.administerManagedObject(object);

		// Add the team
		architect.addOfficeTeam("team");

		// Add the input
		architect.addOfficeInput("INPUT", Integer.class.getName());

		// Add the output
		architect.addOfficeOutput("OUTPUT", Long.class.getName());
	}

	/**
	 * Mock class for the {@link ClassSectionSource}.
	 */
	public static class MockSection {
		public void input(@Parameter String parameter) {
		}
	}

	/**
	 * Mock class for the {@link ClassAdministrationSource}.
	 */
	public static class MockAdministrator {
		public void admin(XAResource[] resources) {
		}
	}

}
