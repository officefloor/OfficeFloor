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
package net.officefloor.compile.test.office;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.type.TypeContextImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Utility class for testing an {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeLoaderUtil {

	/**
	 * {@link Office} location for loading.
	 */
	private static final String OFFICE_LOCATION = OfficeLoaderUtil.class
			.getSimpleName();

	/**
	 * Validates the {@link OfficeSourceSpecification} for the
	 * {@link OfficeSource}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            {@link OfficeSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <O extends OfficeSource> PropertyList validateSpecification(
			Class<O> officeSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList properties = getOfficeFloorCompiler().getOfficeLoader()
				.loadSpecification(officeSourceClass);

		// Validate the properties of the specification
		PropertyListUtil.validatePropertyNameLabels(properties,
				propertyNameLabels);

		// Return the specification properties
		return properties;
	}

	/**
	 * Convenience method to obtain the class path location.
	 * 
	 * @param offsetClass
	 *            Class indicating the package that the resource is within.
	 *            Typically this will be the {@link TestCase} instance.
	 * @param resourceName
	 *            Name of the resource.
	 * @return Class path location of the resource.
	 */
	public static String getClassPathLocation(Class<?> offsetClass,
			String resourceName) {
		return SectionLoaderUtil
				.getClassPathLocation(offsetClass, resourceName);
	}

	/**
	 * Creates the {@link OfficeArchitect} to create the expected
	 * {@link OfficeType}.
	 * 
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @return {@link OfficeArchitect}.
	 */
	public static OfficeArchitect createOfficeArchitect(
			String officeSourceClassName) {
		OfficeFloorCompiler compiler = getOfficeFloorCompiler();
		NodeContext context = (NodeContext) compiler;
		OfficeNode office = new OfficeNodeImpl(
				OfficeLoaderUtil.class.getSimpleName(), null, context);
		office.initialise(officeSourceClassName, null, OFFICE_LOCATION);
		return office;
	}

	/**
	 * Creates the {@link OfficeArchitect} to create the expected
	 * {@link OfficeType}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            {@link OfficeSource} instance.
	 * @return {@link OfficeArchitect}.
	 */
	public static <O extends OfficeSource> OfficeArchitect createOfficeArchitect(
			O officeSource) {
		OfficeFloorCompiler compiler = getOfficeFloorCompiler();
		NodeContext context = (NodeContext) compiler;
		OfficeNode office = new OfficeNodeImpl(
				OfficeLoaderUtil.class.getSimpleName(), null, context);
		office.initialise(officeSource.getClass().getName(), officeSource,
				OFFICE_LOCATION);
		return office;
	}

	/**
	 * Convenience method to validate the {@link OfficeType} via an offset
	 * object to locate the {@link Office}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param architect
	 *            {@link OfficeArchitect} containing the expected
	 *            {@link OfficeType}.
	 * @param officeSourceClass
	 *            {@link OfficeSource} class.
	 * @param offsetClass
	 *            Class indicating the package that the resource is within.
	 *            Typically this will be the {@link TestCase} instance.
	 * @param resourceName
	 *            Name of the resource for the {@link Office} location.
	 * @param propertyNameValuePairs
	 *            {@link Property} name/value listings.
	 * @return {@link OfficeType}.
	 */
	public static <O extends OfficeSource> OfficeType validateOffice(
			OfficeArchitect architect, Class<O> officeSourceClass,
			Class<?> offsetClass, String resourceName,
			String... propertyNameValuePairs) {

		// Obtain the location of the office
		String officeLocation = getClassPathLocation(offsetClass, resourceName);

		// Validate the office type
		return validateOffice(architect, officeSourceClass, officeLocation,
				propertyNameValuePairs);
	}

	/**
	 * Validates the loaded {@link OfficeType} against the expected
	 * {@link OfficeType} from the {@link OfficeArchitect}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param architect
	 *            {@link OfficeArchitect} containing the expected
	 *            {@link OfficeType}.
	 * @param officeSourceClass
	 *            {@link OfficeSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name/value listings.
	 * @return {@link OfficeType}.
	 */
	public static <O extends OfficeSource> OfficeType validateOffice(
			OfficeArchitect architect, Class<O> officeSourceClass,
			String officeLocation, String... propertyNameValuePairs) {

		// Cast to obtain expected office type
		if (!(architect instanceof OfficeNode)) {
			TestCase.fail("architect must be created from createOfficeArchitect");
		}
		OfficeType expectedOffice = ((OfficeNode) architect)
				.loadOfficeType(new TypeContextImpl());

		// Load the actual office type
		OfficeType actualOffice = loadOfficeType(officeSourceClass,
				officeLocation, propertyNameValuePairs);

		// Validate the section inputs
		OfficeAvailableSectionInputType[] eSectionInputs = expectedOffice
				.getOfficeSectionInputTypes();
		OfficeAvailableSectionInputType[] aSectionInputs = actualOffice
				.getOfficeSectionInputTypes();
		TestCase.assertEquals("Incorrect number of section inputs",
				eSectionInputs.length, aSectionInputs.length);
		for (int i = 0; i < eSectionInputs.length; i++) {
			OfficeAvailableSectionInputType eSectionInput = eSectionInputs[i];
			OfficeAvailableSectionInputType aSectionInput = aSectionInputs[i];
			TestCase.assertEquals("Incorrect name for section input " + i,
					eSectionInput.getOfficeSectionInputName(),
					aSectionInput.getOfficeSectionInputName());
			TestCase.assertEquals("Incorrect section for section input " + i,
					eSectionInput.getOfficeSectionName(),
					aSectionInput.getOfficeSectionName());
			TestCase.assertEquals("Incorrect parameter type for section input "
					+ i, eSectionInput.getParameterType(),
					aSectionInput.getParameterType());
		}

		// Validate the inputs
		OfficeInputType[] eInputs = expectedOffice.getOfficeInputTypes();
		OfficeInputType[] aInputs = actualOffice.getOfficeInputTypes();
		TestCase.assertEquals("Incorrect number of inputs", eInputs.length,
				aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			OfficeInputType eInput = eInputs[i];
			OfficeInputType aInput = aInputs[i];
			TestCase.assertEquals("Incorrect name for input " + i,
					eInput.getOfficeInputName(), aInput.getOfficeInputName());
			TestCase.assertEquals("Incorrect parameter type for input " + i,
					eInput.getParameterType(), aInput.getParameterType());
			OfficeOutputType eResponse = eInput.getResponseOfficeOutputType();
			if (eResponse == null) {
				TestCase.assertNull(
						"Should not have response output for input " + i,
						aInput.getResponseOfficeOutputType());
			} else {
				OfficeOutputType aResponse = aInput
						.getResponseOfficeOutputType();
				TestCase.assertEquals(
						"Incorrect response output for input " + i,
						eResponse.getOfficeOutputName(),
						(aResponse == null ? null : aResponse
								.getOfficeOutputName()));
			}
		}

		// Validate the outputs
		OfficeOutputType[] eOutputs = expectedOffice.getOfficeOutputTypes();
		OfficeOutputType[] aOutputs = actualOffice.getOfficeOutputTypes();
		TestCase.assertEquals("Incorrect number of outputs", eOutputs.length,
				aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			OfficeOutputType eOutput = eOutputs[i];
			OfficeOutputType aOutput = aOutputs[i];
			TestCase.assertEquals("Incorrect name for output " + i,
					eOutput.getOfficeOutputName(),
					aOutput.getOfficeOutputName());
			TestCase.assertEquals("Incorrect argument type for output " + i,
					eOutput.getArgumentType(), aOutput.getArgumentType());
			OfficeInputType eHandler = eOutput.getHandlingOfficeInputType();
			if (eHandler == null) {
				TestCase.assertEquals(
						"Should not have handler input for output " + i,
						aOutput.getHandlingOfficeInputType());
			} else {
				OfficeInputType aHandler = aOutput.getHandlingOfficeInputType();
				TestCase.assertEquals(
						"Incorrect handler input for output " + i, eHandler
								.getOfficeInputName(), (aHandler == null ? null
								: aHandler.getOfficeInputName()));
			}
		}

		// Validate the managed objects
		OfficeManagedObjectType[] eManagedObjects = expectedOffice
				.getOfficeManagedObjectTypes();
		OfficeManagedObjectType[] aManagedObjects = actualOffice
				.getOfficeManagedObjectTypes();
		TestCase.assertEquals("Incorrect number of managed objects",
				eManagedObjects.length, aManagedObjects.length);
		for (int i = 0; i < eManagedObjects.length; i++) {
			OfficeManagedObjectType eManagedObject = eManagedObjects[i];
			OfficeManagedObjectType aManagedObject = aManagedObjects[i];
			TestCase.assertEquals("Incorrect name for managed object " + i,
					eManagedObject.getOfficeManagedObjectName(),
					aManagedObject.getOfficeManagedObjectName());
			TestCase.assertEquals("Incorrect object type for managed object "
					+ i, eManagedObject.getObjectType(),
					aManagedObject.getObjectType());

			// Validate the supported extension interfaces
			String[] eEis = eManagedObject.getExtensionInterfaces();
			String[] aEis = aManagedObject.getExtensionInterfaces();
			TestCase.assertEquals(
					"Incorrect number of extension interfaces for managed object "
							+ i, eEis.length, aEis.length);
			for (int j = 0; j < eEis.length; j++) {
				TestCase.assertEquals("Incorrect extension interface " + j,
						eEis[j], aEis[j]);
			}
		}

		// Validate the teams
		OfficeTeamType[] eTeams = expectedOffice.getOfficeTeamTypes();
		OfficeTeamType[] aTeams = actualOffice.getOfficeTeamTypes();
		TestCase.assertEquals("Incorrect number of teams", eTeams.length,
				aTeams.length);
		for (int i = 0; i < eTeams.length; i++) {
			OfficeTeamType eTeam = eTeams[i];
			OfficeTeamType aTeam = aTeams[i];
			TestCase.assertEquals("Incorrect team " + i,
					eTeam.getOfficeTeamName(), aTeam.getOfficeTeamName());
		}

		// Return the actual office type
		return actualOffice;
	}

	/**
	 * Convenience method to use the {@link ClassLoader} of the
	 * {@link OfficeSource} class and {@link ClassLoaderConfigurationContext} to
	 * load the {@link OfficeType}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            {@link OfficeSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name/value listing.
	 * @return {@link OfficeType}.
	 */
	public static <O extends OfficeSource> OfficeType loadOfficeType(
			Class<O> officeSourceClass, String officeLocation,
			String... propertyNameValuePairs) {

		// Obtain the class loader and configuration context
		ClassLoader classLoader = officeSourceClass.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Return the loaded office type
		return loadOfficeType(officeSourceClass, officeLocation,
				configurationContext, classLoader, propertyNameValuePairs);
	}

	/**
	 * Loads the {@link OfficeType}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            {@link OfficeSource} class.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name/value listing.
	 * @return {@link OfficeType}.
	 */
	public static <O extends OfficeSource> OfficeType loadOfficeType(
			Class<O> officeSourceClass, String officeLocation,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			String... propertyNameValuePairs) {

		// Return the loaded office type
		return getOfficeFloorCompiler().getOfficeLoader().loadOfficeType(
				officeSourceClass, officeLocation,
				new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the office floor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private OfficeLoaderUtil() {
	}
}