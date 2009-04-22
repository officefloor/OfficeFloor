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
package net.officefloor.compile.test.section;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.section.SectionLoaderImpl;
import net.officefloor.compile.impl.structure.NodeContextImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Utility class for testing a {@link SectionSource}.
 * 
 * @author Daniel
 */
public class SectionLoaderUtil {

	/**
	 * Name of the {@link OfficeSection} used in testing.
	 */
	public static final String SECTION_NAME = "TEST";

	/**
	 * Validates the {@link SectionSourceSpecification} for the
	 * {@link SectionSource}.
	 * 
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends SectionSource> PropertyList validateSpecification(
			Class<S> sectionSourceClass, String... propertyNameLabels) {

		// Create the section loader
		SectionLoader sectionLoader = new SectionLoaderImpl();

		// Load the specification
		PropertyList propertyList = sectionLoader.loadSpecification(
				sectionSourceClass, new FailCompilerIssues());

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Convenience method to obtain the class path location.
	 * 
	 * @param offsetObject
	 *            Object indicating the package that the resource is within.
	 *            Typically this will be the {@link TestCase} instance.
	 * @param resourceName
	 *            Name of the resource.
	 * @return Class path location of the resource.
	 */
	public static String getClassPathLocation(Object offsetObject,
			String resourceName) {

		// Obtain the package location
		String packageLocation = offsetObject.getClass().getPackage().getName();
		packageLocation = packageLocation.replace('.', '/');

		// Provide the class path location
		String classpathLocation = packageLocation + "/" + resourceName;
		return classpathLocation;
	}

	/**
	 * Creates the {@link SectionDesigner} to create the expected
	 * {@link SectionType}/{@link OfficeSection}.
	 * 
	 * @param sectionSourceClass
	 *            {@link SectionSource} class being tested.
	 * @return {@link SectionDesigner}.
	 */
	public static <S extends SectionSource> SectionDesigner createSectionDesigner(
			Class<S> sectionSourceClass) {
		NodeContext context = new NodeContextImpl(sectionSourceClass
				.getClassLoader(), new FailCompilerIssues());
		return new SectionNodeImpl(SECTION_NAME, "TEST_LOCATION", context);
	}

	/**
	 * Facade method to validate the {@link SectionType} and
	 * {@link OfficeSection}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} containing the expected
	 *            {@link SectionType}/{@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource} being tested.
	 * @param offsetObject
	 *            Object indicating the package that the resource is within.
	 *            Typically this will be the {@link TestCase} instance.
	 * @param resourceName
	 *            Name of the resource. This is used with the
	 *            <code>offsetObject</code> to determine the
	 *            {@link OfficeSection} location.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateSection(
			SectionDesigner designer, Class<S> sectionSourceClass,
			Object offsetObject, String resourceName,
			String... propertyNameValuePairs) {

		// Obtain the section location
		String sectionLocation = getClassPathLocation(offsetObject,
				resourceName);

		// Validate the section
		validateSection(designer, sectionSourceClass, sectionLocation,
				propertyNameValuePairs);
	}

	/**
	 * Convenience method that validates both the loaded {@link SectionType} and
	 * {@link OfficeSection} against expected {@link SectionType}/
	 * {@link OfficeSection} from the {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} containing the expected
	 *            {@link SectionType}/{@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource} being tested.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateSection(
			SectionDesigner designer, Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Validate the section type
		validateSectionType(designer, sectionSourceClass, sectionLocation,
				propertyNameValuePairs);

		// Validate the office section
		validateOfficeSection(designer, sectionSourceClass, sectionLocation,
				propertyNameValuePairs);
	}

	/**
	 * Convenience method that validates the loaded {@link SectionType} against
	 * expected {@link SectionType} from the {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} containing the expected
	 *            {@link SectionType}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource} being tested.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateSectionType(
			SectionDesigner designer, Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Cast to obtain expected section type
		if (!(designer instanceof SectionType)) {
			TestCase
					.fail("designer must be created from createSectionDesigner");
		}
		SectionType expectedSection = (SectionType) designer;

		// Load the actual section type
		SectionType actualSection = loadSectionType(sectionSourceClass,
				sectionLocation, propertyNameValuePairs);

		// Validate section inputs are as expected
		SectionInputType[] eInputs = expectedSection.getSectionInputTypes();
		SectionInputType[] aInputs = actualSection.getSectionInputTypes();
		TestCase.assertEquals("Incorrect number of inputs", eInputs.length,
				aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			SectionInputType eInput = eInputs[i];
			SectionInputType aInput = aInputs[i];
			TestCase.assertEquals("Incorrect name for input " + i, eInput
					.getSectionInputName(), aInput.getSectionInputName());
			TestCase.assertEquals("Incorrect parameter type for input " + i,
					eInput.getParameterType(), aInput.getParameterType());
		}

		// Validate the section outputs are as expected
		SectionOutputType[] eOutputs = expectedSection.getSectionOutputTypes();
		SectionOutputType[] aOutputs = actualSection.getSectionOutputTypes();
		TestCase.assertEquals("Incorrect number of outputs", eOutputs.length,
				aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			SectionOutputType eOutput = eOutputs[i];
			SectionOutputType aOutput = aOutputs[i];
			TestCase.assertEquals("Incorrect name for output " + i, eOutput
					.getSectionOutputName(), aOutput.getSectionOutputName());
			TestCase.assertEquals("Incorrect argument type for output " + i,
					eOutput.getArgumentType(), aOutput.getArgumentType());
			TestCase.assertEquals("Incorrect escalation only for output " + i,
					eOutput.isEscalationOnly(), aOutput.isEscalationOnly());
		}

		// Validate the section objects are as expected
		SectionObjectType[] eObjects = expectedSection.getSectionObjectTypes();
		SectionObjectType[] aObjects = actualSection.getSectionObjectTypes();
		TestCase.assertEquals("Incorrect number of objects", eObjects.length,
				aObjects.length);
		for (int i = 0; i < eObjects.length; i++) {
			SectionObjectType eObject = eObjects[i];
			SectionObjectType aObject = aObjects[i];
			TestCase.assertEquals("Incorrect name for object " + i, eObject
					.getSectionObjectName(), aObject.getSectionObjectName());
			TestCase.assertEquals("Incorrect object type for input " + i,
					eObject.getObjectType(), aObject.getObjectType());
		}
	}

	/**
	 * Convenience method that validates the loaded {@link OfficeSection}
	 * against expected {@link OfficeSection} from the {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} containing the expected
	 *            {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource} being tested.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateOfficeSection(
			SectionDesigner designer, Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Cast to obtain expected section type
		if (!(designer instanceof OfficeSection)) {
			TestCase
					.fail("designer must be created from createSectionDesigner");
		}
		OfficeSection eSection = (OfficeSection) designer;

		// Load the actual section type
		OfficeSection aSection = loadOfficeSection(SECTION_NAME,
				sectionSourceClass, sectionLocation, propertyNameValuePairs);

		// Validate the office section
		TestCase.assertEquals("Incorrect section name", eSection
				.getOfficeSectionName(), aSection.getOfficeSectionName());

		// Validate the office section inputs
		OfficeSectionInput[] eInputs = eSection.getOfficeSectionInputs();
		OfficeSectionInput[] aInputs = aSection.getOfficeSectionInputs();
		TestCase.assertEquals("Incorrect number of section inputs",
				eInputs.length, aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			OfficeSectionInput eInput = eInputs[i];
			OfficeSectionInput aInput = aInputs[i];
			TestCase.assertEquals("Incorrect name for section input " + i,
					eInput.getOfficeSectionInputName(), aInput
							.getOfficeSectionInputName());
			TestCase.assertEquals("Incorrect parameter type for section input "
					+ i, eInput.getParameterType(), aInput.getParameterType());
		}

		// Validate the office section outputs
		OfficeSectionOutput[] eOutputs = eSection.getOfficeSectionOutputs();
		OfficeSectionOutput[] aOutputs = aSection.getOfficeSectionOutputs();
		TestCase.assertEquals("Incorrect number of section outputs",
				eOutputs.length, aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			OfficeSectionOutput eOutput = eOutputs[i];
			OfficeSectionOutput aOutput = aOutputs[i];
			TestCase.assertEquals("Incorrect name for section output " + i,
					eOutput.getOfficeSectionOutputName(), aOutput
							.getOfficeSectionOutputName());
			TestCase.assertEquals("Incorrect argument type for section output "
					+ i, eOutput.getArgumentType(), aOutput.getArgumentType());
			TestCase.assertEquals(
					"Incorrect escalation only for section output " + i,
					eOutput.isEscalationOnly(), aOutput.isEscalationOnly());
		}

		// Validate the office section objects
		OfficeSectionObject[] eObjects = eSection.getOfficeSectionObjects();
		OfficeSectionObject[] aObjects = aSection.getOfficeSectionObjects();
		TestCase.assertEquals("Incorrect number of section objects",
				eObjects.length, aObjects.length);
		for (int i = 0; i < eObjects.length; i++) {
			OfficeSectionObject eObject = eObjects[i];
			OfficeSectionObject aObject = aObjects[i];
			TestCase.assertEquals("Incorrect name for section object " + i,
					eObject.getOfficeSectionObjectName(), aObject
							.getOfficeSectionObjectName());
			TestCase.assertEquals("Incorrect object type for section object "
					+ i, eObject.getObjectType(), aObject.getObjectType());
		}

		// Validate remaining of the office section
		validateOfficeSubSection(eSection, aSection);
	}

	/**
	 * Validates the {@link OfficeSubSection}.
	 * 
	 * @param eSection
	 *            Expected {@link OfficeSubSection}.
	 * @param aSection
	 *            Actual {@link OfficeSubSection}.
	 */
	private static void validateOfficeSubSection(OfficeSubSection eSection,
			OfficeSubSection aSection) {

		// Validate the office sub section
		TestCase.assertEquals("Incorrect section name", eSection
				.getOfficeSectionName(), aSection.getOfficeSectionName());

		// Validate the tasks
		OfficeTask[] eTasks = eSection.getOfficeTasks();
		OfficeTask[] aTasks = aSection.getOfficeTasks();
		TestCase.assertEquals("Incorrect number of tasks", eTasks.length,
				aTasks.length);
		for (int i = 0; i < eTasks.length; i++) {
			OfficeTask eTask = eTasks[i];
			OfficeTask aTask = aTasks[i];
			TestCase.assertEquals("Incorrect name for task " + i, eTask
					.getOfficeTaskName(), aTask.getOfficeTaskName());
			TestCase.assertNotNull("Must have team responsible for task " + i,
					aTask.getTeamResponsible());

			// Validate the dependencies
			ObjectDependency[] eDependencies = eTask.getObjectDependencies();
			ObjectDependency[] aDependencies = aTask.getObjectDependencies();
			TestCase.assertEquals("Incorrect number of dependencies for task "
					+ i, eDependencies.length, aDependencies.length);
			for (int j = 0; j < eDependencies.length; j++) {
				ObjectDependency eDependency = eDependencies[j];
				ObjectDependency aDependency = aDependencies[j];
				TestCase.assertEquals("Incorrect name for dependency " + j,
						eDependency.getObjectDependencyName(), aDependency
								.getObjectDependencyName());
				DependentManagedObject eMo = eDependency
						.getDependentManagedObject();
				DependentManagedObject aMo = aDependency
						.getDependentManagedObject();
				TestCase.assertEquals(
						"Incorrect dependent name for dependency " + j, eMo
								.getDependentManagedObjectName(), aMo
								.getDependentManagedObjectName());

			}
		}

		// Validate the managed objects
		OfficeSectionManagedObject[] eMos = eSection
				.getOfficeSectionManagedObjects();
		OfficeSectionManagedObject[] aMos = aSection
				.getOfficeSectionManagedObjects();
		TestCase.assertEquals("Incorrect number of managed objects",
				eMos.length, aMos.length);
		for (int i = 0; i < eMos.length; i++) {
			OfficeSectionManagedObject eMo = eMos[i];
			OfficeSectionManagedObject aMo = aMos[i];
			TestCase.assertEquals("Incorrect name for managed obect " + i, eMo
					.getOfficeSectionManagedObjectName(), aMo
					.getOfficeSectionManagedObjectName());
			TestCase.assertEquals(
					"Incorrect dependent name for managed object " + i, eMo
							.getDependentManagedObjectName(), aMo
							.getDependentManagedObjectName());
			TestCase.assertEquals(
					"Incorrect administerable name for managed object " + i,
					eMo.getAdministerableManagedObjectName(), aMo
							.getAdministerableManagedObjectName());

			// Validate the managed object teams
			ManagedObjectTeam[] eTeams = eMo
					.getOfficeSectionManagedObjectTeams();
			ManagedObjectTeam[] aTeams = aMo
					.getOfficeSectionManagedObjectTeams();
			TestCase.assertEquals(
					"Incorrect number of teams for managed object " + i,
					eTeams.length, aTeams.length);
			for (int j = 0; j < eTeams.length; j++) {
				ManagedObjectTeam eTeam = eTeams[j];
				ManagedObjectTeam aTeam = aTeams[j];
				TestCase.assertEquals("Incorrect name for team " + j, eTeam
						.getManagedObjectTeamName(), aTeam
						.getManagedObjectTeamName());
			}

			// Validate the managed object supported extension interfaces
			Class<?>[] eEis = eMo.getSupportedExtensionInterfaces();
			Class<?>[] aEis = aMo.getSupportedExtensionInterfaces();
			TestCase.assertEquals(
					"Incorrect number of supported extension interfaces for managed object "
							+ i, eEis.length, aEis.length);
			for (int j = 0; j < eEis.length; j++) {
				TestCase.assertEquals(
						"Incorrect class for extension interface " + j,
						eEis[j], aEis[j]);
			}
		}

		// Validate the sub sections
		OfficeSubSection[] eSubSections = eSection.getOfficeSubSections();
		OfficeSubSection[] aSubSections = aSection.getOfficeSubSections();
		TestCase.assertEquals("Incorect number of sub sections",
				eSubSections.length, aSubSections.length);
		for (int i = 0; i < eSubSections.length; i++) {
			validateOfficeSubSection(eSubSections[i], aSubSections[i]);
		}
	}

	/**
	 * Convenience method to load the {@link SectionType}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 */
	public static <S extends SectionSource> SectionType loadSectionType(
			Class<S> sectionSourceClass, String sectionLocation,
			String... propertyNameValuePairs) {

		// Obtain the class loader
		ClassLoader classLoader = sectionSourceClass.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		try {
			// Load and return the section type
			return loadSectionType(sectionSourceClass, sectionLocation,
					configurationContext, classLoader, propertyNameValuePairs);

		} catch (Exception ex) {
			// Propagate as test case failure for tests not needing to handle
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			TestCase.fail(stackTrace.toString());
			return null; // fail will propagate failure
		}
	}

	/**
	 * Loads the {@link SectionType}.
	 * 
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 * @throws Exception
	 *             If fails to load the {@link SectionType}.
	 */
	public static <S extends SectionSource> SectionType loadSectionType(
			Class<S> sectionSourceClass, String sectionLocation,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			String... propertyNameValuePairs) throws Exception {

		// Create the section loader
		SectionLoader sectionLoader = new SectionLoaderImpl();

		// Load and return the section type
		return sectionLoader.loadSectionType(sectionSourceClass,
				sectionLocation, new PropertyListImpl(propertyNameValuePairs),
				configurationContext, classLoader, new FailCompilerIssues());
	}

	/**
	 * Convenience method to load the {@link OfficeSection}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 */
	public static <S extends SectionSource> OfficeSection loadOfficeSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Obtain the class loader
		ClassLoader classLoader = sectionSourceClass.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Load and return the office section
		return loadOfficeSection(sectionName, sectionSourceClass,
				sectionLocation, configurationContext, classLoader,
				propertyNameValuePairs);
	}

	/**
	 * Loads the {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link OfficeSection}.
	 */
	public static <S extends SectionSource> OfficeSection loadOfficeSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, ConfigurationContext configurationContext,
			ClassLoader classLoader, String... propertyNameValuePairs) {

		// Create the section loader
		SectionLoader sectionLoader = new SectionLoaderImpl();

		// Load and return the office section
		return sectionLoader.loadOfficeSection(sectionName, sectionSourceClass,
				sectionLocation, new PropertyListImpl(propertyNameValuePairs),
				configurationContext, classLoader, new FailCompilerIssues());
	}

	/**
	 * All access via static methods.
	 */
	private SectionLoaderUtil() {
	}

}