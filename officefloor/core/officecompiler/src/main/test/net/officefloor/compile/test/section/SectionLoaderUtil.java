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
package net.officefloor.compile.test.section;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.function.IntFunction;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.type.TypeContextImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Utility class for testing a {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionLoaderUtil {

	/**
	 * Validates the {@link SectionSourceSpecification} for the
	 * {@link SectionSource}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends SectionSource> PropertyList validateSpecification(Class<S> sectionSourceClass,
			String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler().getSectionLoader().loadSpecification(sectionSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
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
	public static String getClassPathLocation(Class<?> offsetClass, String resourceName) {

		// Obtain the package location
		String packageLocation = offsetClass.getPackage().getName();
		packageLocation = packageLocation.replace('.', '/');

		// Provide the class path location
		String classpathLocation = packageLocation + "/" + resourceName;
		return classpathLocation;
	}

	/**
	 * Creates the {@link SectionDesigner} to create the expected
	 * {@link SectionType}/{@link OfficeSection}.
	 * 
	 * @return {@link SectionDesigner}.
	 */
	public static SectionDesigner createSectionDesigner() {
		OfficeFloorCompiler compiler = getOfficeFloorCompiler();
		NodeContext context = (NodeContext) compiler;
		return context.createSectionNode(SectionLoaderUtil.class.getSimpleName(), (OfficeNode) null);
	}

	/**
	 * Facade method to validate the {@link SectionType} and
	 * {@link OfficeSection}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param designer
	 *            {@link SectionDesigner} containing the expected
	 *            {@link SectionType}/{@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource} being tested.
	 * @param offsetClass
	 *            Object indicating the package that the resource is within.
	 *            Typically this will be the {@link TestCase} instance.
	 * @param resourceName
	 *            Name of the resource. This is used with the
	 *            <code>offsetObject</code> to determine the
	 *            {@link OfficeSection} location.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateSection(SectionDesigner designer, Class<S> sectionSourceClass,
			Class<?> offsetClass, String resourceName, String... propertyNameValuePairs) {

		// Obtain the section location
		String sectionLocation = getClassPathLocation(offsetClass, resourceName);

		// Validate the section
		validateSection(designer, sectionSourceClass, sectionLocation, propertyNameValuePairs);
	}

	/**
	 * Convenience method that validates both the loaded {@link SectionType} and
	 * {@link OfficeSection} against expected {@link SectionType}/
	 * {@link OfficeSection} from the {@link SectionDesigner}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
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
	public static <S extends SectionSource> void validateSection(SectionDesigner designer, Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Validate the section type
		validateSectionType(designer, sectionSourceClass, sectionLocation, propertyNameValuePairs);

		// Validate the office section
		validateOfficeSection(designer, sectionSourceClass, sectionLocation, propertyNameValuePairs);
	}

	/**
	 * Convenience method that validates the loaded {@link SectionType} against
	 * expected {@link SectionType} from the {@link SectionDesigner}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
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
	public static <S extends SectionSource> void validateSectionType(SectionDesigner designer,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

		// Cast to obtain expected section type
		if (!(designer instanceof SectionNode)) {
			TestCase.fail("designer must be created from createSectionDesigner");
		}
		SectionType expectedSection = ((SectionNode) designer).loadSectionType(new TypeContextImpl());

		// Load the actual section type
		SectionType actualSection = loadSectionType(sectionSourceClass, sectionLocation, propertyNameValuePairs);

		// Validate section inputs are as expected
		SectionInputType[] eInputs = expectedSection.getSectionInputTypes();
		SectionInputType[] aInputs = actualSection.getSectionInputTypes();
		TestCase.assertEquals("Incorrect number of inputs", eInputs.length, aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			SectionInputType eInput = eInputs[i];
			SectionInputType aInput = aInputs[i];
			TestCase.assertEquals("Incorrect name for input " + i, eInput.getSectionInputName(),
					aInput.getSectionInputName());
			TestCase.assertEquals("Incorrect parameter type for input " + i, eInput.getParameterType(),
					aInput.getParameterType());
		}

		// Validate the section outputs are as expected
		SectionOutputType[] eOutputs = expectedSection.getSectionOutputTypes();
		SectionOutputType[] aOutputs = actualSection.getSectionOutputTypes();
		TestCase.assertEquals("Incorrect number of outputs", eOutputs.length, aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			SectionOutputType eOutput = eOutputs[i];
			SectionOutputType aOutput = aOutputs[i];
			TestCase.assertEquals("Incorrect name for output " + i, eOutput.getSectionOutputName(),
					aOutput.getSectionOutputName());
			TestCase.assertEquals("Incorrect argument type for output " + i, eOutput.getArgumentType(),
					aOutput.getArgumentType());
			TestCase.assertEquals("Incorrect escalation only for output " + i, eOutput.isEscalationOnly(),
					aOutput.isEscalationOnly());
		}

		// Validate the section objects are as expected
		SectionObjectType[] eObjects = expectedSection.getSectionObjectTypes();
		SectionObjectType[] aObjects = actualSection.getSectionObjectTypes();
		TestCase.assertEquals("Incorrect number of objects", eObjects.length, aObjects.length);
		for (int i = 0; i < eObjects.length; i++) {
			SectionObjectType eObject = eObjects[i];
			SectionObjectType aObject = aObjects[i];
			TestCase.assertEquals("Incorrect name for object " + i, eObject.getSectionObjectName(),
					aObject.getSectionObjectName());
			TestCase.assertEquals("Incorrect object type for object " + i, eObject.getObjectType(),
					aObject.getObjectType());
			TestCase.assertEquals("Incorrect type qualifier for object " + i, eObject.getTypeQualifier(),
					aObject.getTypeQualifier());
		}
	}

	/**
	 * Convenience method that validates the loaded {@link OfficeSection}
	 * against expected {@link OfficeSection} from the {@link SectionDesigner}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
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
	public static <S extends SectionSource> void validateOfficeSection(SectionDesigner designer,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

		// Cast to obtain expected section type
		if (!(designer instanceof SectionNode)) {
			TestCase.fail("designer must be created from createSectionDesigner");
		}
		SectionNode section = (SectionNode) designer;
		section.initialise(sectionSourceClass.getName(), null, sectionLocation);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			section.addProperty(name, value);
		}
		OfficeSectionType eSection = section.loadOfficeSectionType(new TypeContextImpl());

		// Load the actual section type
		OfficeSectionType aSection = loadOfficeSectionType(SectionLoaderUtil.class.getSimpleName(), sectionSourceClass,
				sectionLocation, propertyNameValuePairs);

		// Validate the office section
		TestCase.assertEquals("Incorrect section name", eSection.getOfficeSectionName(),
				aSection.getOfficeSectionName());

		// Validate the office section inputs
		OfficeSectionInputType[] eInputs = eSection.getOfficeSectionInputTypes();
		OfficeSectionInputType[] aInputs = aSection.getOfficeSectionInputTypes();
		TestCase.assertEquals("Incorrect number of section inputs", eInputs.length, aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			OfficeSectionInputType eInput = eInputs[i];
			OfficeSectionInputType aInput = aInputs[i];
			TestCase.assertEquals("Incorrect name for section input " + i, eInput.getOfficeSectionInputName(),
					aInput.getOfficeSectionInputName());
			TestCase.assertEquals("Incorrect parameter type for section input " + i, eInput.getParameterType(),
					aInput.getParameterType());
		}

		// Validate the office section outputs
		OfficeSectionOutputType[] eOutputs = eSection.getOfficeSectionOutputTypes();
		OfficeSectionOutputType[] aOutputs = aSection.getOfficeSectionOutputTypes();
		TestCase.assertEquals("Incorrect number of section outputs", eOutputs.length, aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			OfficeSectionOutputType eOutput = eOutputs[i];
			OfficeSectionOutputType aOutput = aOutputs[i];
			TestCase.assertEquals("Incorrect name for section output " + i, eOutput.getOfficeSectionOutputName(),
					aOutput.getOfficeSectionOutputName());
			TestCase.assertEquals("Incorrect argument type for section output " + i, eOutput.getArgumentType(),
					aOutput.getArgumentType());
			TestCase.assertEquals("Incorrect escalation only for section output " + i, eOutput.isEscalationOnly(),
					aOutput.isEscalationOnly());
		}

		// Validate the office section objects
		OfficeSectionObjectType[] eObjects = eSection.getOfficeSectionObjectTypes();
		OfficeSectionObjectType[] aObjects = aSection.getOfficeSectionObjectTypes();
		TestCase.assertEquals("Incorrect number of section objects", eObjects.length, aObjects.length);
		for (int i = 0; i < eObjects.length; i++) {
			OfficeSectionObjectType eObject = eObjects[i];
			OfficeSectionObjectType aObject = aObjects[i];
			TestCase.assertEquals("Incorrect name for section object " + i, eObject.getOfficeSectionObjectName(),
					aObject.getOfficeSectionObjectName());
			TestCase.assertEquals("Incorrect object type for section object " + i, eObject.getObjectType(),
					aObject.getObjectType());
			TestCase.assertEquals("Incorrect type qualifier for section object " + i, eObject.getTypeQualifier(),
					aObject.getTypeQualifier());
		}

		// Validate remaining of the office section
		validateOfficeSubSectionType(null, eSection, aSection);
	}

	/**
	 * Validates the {@link OfficeSubSection}.
	 * 
	 * @param subSectionName
	 *            Name of the {@link OfficeSubSection} being validated.
	 * @param eSection
	 *            Expected {@link OfficeSubSection}.
	 * @param aSection
	 *            Actual {@link OfficeSubSection}.
	 */
	private static void validateOfficeSubSectionType(String subSectionName, OfficeSubSectionType eSection,
			OfficeSubSectionType aSection) {

		// Validate the office sub section
		TestCase.assertEquals("Incorrect section name (parent section=" + subSectionName + ")",
				eSection.getOfficeSectionName(), aSection.getOfficeSectionName());

		// Determine this sub section name
		subSectionName = (subSectionName == null ? "" : subSectionName + ".") + eSection.getOfficeSectionName();

		// Validate the tasks
		OfficeTaskType[] eTasks = eSection.getOfficeTaskTypes();
		OfficeTaskType[] aTasks = aSection.getOfficeTaskTypes();
		IntFunction<String> tasksLister = createCompareLister(eTasks, aTasks,
				(taskType) -> taskType.getOfficeTaskName());
		TestCase.assertEquals("Incorrect number of tasks (section=" + subSectionName + ")" + tasksLister.apply(-1),
				eTasks.length, aTasks.length);
		for (int i = 0; i < eTasks.length; i++) {
			OfficeTaskType eTask = eTasks[i];
			OfficeTaskType aTask = aTasks[i];
			TestCase.assertEquals(
					"Incorrect name for task " + i + " (sub section=" + subSectionName + ")" + tasksLister.apply(i),
					eTask.getOfficeTaskName(), aTask.getOfficeTaskName());

			// Validate the dependencies
			ObjectDependencyType[] eDependencies = eTask.getObjectDependencies();
			ObjectDependencyType[] aDependencies = aTask.getObjectDependencies();
			IntFunction<String> dependencyLIster = createCompareLister(eDependencies, aDependencies,
					(dependency) -> dependency.getObjectDependencyName());
			TestCase.assertEquals(
					"Incorrect number of dependencies for task " + i + " (sub section=" + subSectionName + ", task="
							+ eTask.getOfficeTaskName() + ")" + dependencyLIster.apply(-1),
					eDependencies.length, aDependencies.length);
			for (int j = 0; j < eDependencies.length; j++) {
				ObjectDependencyType eDependency = eDependencies[j];
				ObjectDependencyType aDependency = aDependencies[j];
				TestCase.assertEquals(
						"Incorrect name for dependency " + j + " (sub section=" + subSectionName + ", task="
								+ eTask.getOfficeTaskName() + ")" + dependencyLIster.apply(j),
						eDependency.getObjectDependencyName(), aDependency.getObjectDependencyName());
				// Do not check dependent as requires linking
			}
		}

		// Validate the managed objects
		OfficeSectionManagedObjectType[] eMos = eSection.getOfficeSectionManagedObjectTypes();
		OfficeSectionManagedObjectType[] aMos = aSection.getOfficeSectionManagedObjectTypes();
		IntFunction<String> mosLister = createCompareLister(eMos, aMos,
				(mos) -> mos.getOfficeSectionManagedObjectName());
		TestCase.assertEquals(
				"Incorrect number of managed objects (sub section=" + subSectionName + ")" + mosLister.apply(-1),
				eMos.length, aMos.length);
		for (int j = 0; j < eMos.length; j++) {
			OfficeSectionManagedObjectType eMo = eMos[j];
			OfficeSectionManagedObjectType aMo = aMos[j];
			TestCase.assertEquals("Incorrect name for managed object " + j + " (sub section=" + subSectionName + ")",
					eMo.getOfficeSectionManagedObjectName(), aMo.getOfficeSectionManagedObjectName());
			TestCase.assertEquals(
					"Incorrect dependent name for managed object " + j + " (sub section=" + subSectionName + ")",
					eMo.getDependentObjectName(), aMo.getDependentObjectName());
			String managedObjectName = eMo.getOfficeSectionManagedObjectName();

			// Validate the managed object type qualifiers
			TypeQualification[] eTqs = eMo.getTypeQualifications();
			TypeQualification[] aTqs = aMo.getTypeQualifications();
			IntFunction<String> tqLister = createCompareLister(eTqs, aTqs,
					(tq) -> tq.getQualifier() + ":" + tq.getType());
			TestCase.assertEquals("Incorrect number of type qualifiers for managed object " + j + " (sub section="
					+ subSectionName + ")" + tqLister.apply(-1), eTqs.length, aTqs.length);
			for (int q = 0; q < eTqs.length; q++) {
				TypeQualification eTq = eTqs[q];
				TypeQualification aTq = aTqs[q];
				TestCase.assertEquals(
						"Incorrect qualifying qualifier " + q + " (managed object=" + managedObjectName
								+ ", sub section=" + subSectionName + ")" + tqLister.apply(q),
						eTq.getQualifier(), aTq.getQualifier());
				TestCase.assertEquals("Incorrect qualifying type " + q + " (managed object=" + managedObjectName
						+ ", sub section=" + subSectionName + ")" + tqLister.apply(q), eTq.getType(), aTq.getType());
			}

			// Validate the managed object supported extension interfaces
			Class<?>[] eEis = eMo.getSupportedExtensionInterfaces();
			Class<?>[] aEis = aMo.getSupportedExtensionInterfaces();
			IntFunction<String> eiLister = createCompareLister(eEis, aEis, (ei) -> ei.getName());
			TestCase.assertEquals("Incorrect number of supported extension interfaces for managed object " + j
					+ " (sub section=" + subSectionName + ")" + eiLister.apply(-1), eEis.length, aEis.length);
			for (int k = 0; k < eEis.length; k++) {
				TestCase.assertEquals("Incorrect class for extension interface " + k + " (managed object="
						+ managedObjectName + ", sub section=" + subSectionName + ")" + eiLister.apply(k), eEis[k],
						aEis[k]);
			}

			// Validate the managed object source
			OfficeSectionManagedObjectSourceType eMoSource = eMo.getOfficeSectionManagedObjectSourceType();
			OfficeSectionManagedObjectSourceType aMoSource = aMo.getOfficeSectionManagedObjectSourceType();
			TestCase.assertEquals(
					"Incorrect name for managed obect source " + " (managed object=" + managedObjectName
							+ ", sub section=" + subSectionName + ")",
					eMoSource.getOfficeSectionManagedObjectSourceName(),
					aMoSource.getOfficeSectionManagedObjectSourceName());
			String managedObjectSourceName = eMoSource.getOfficeSectionManagedObjectSourceName();

			// Validate the managed object source teams
			OfficeSectionManagedObjectTeamType[] eTeams = eMoSource.getOfficeSectionManagedObjectTeamTypes();
			OfficeSectionManagedObjectTeamType[] aTeams = aMoSource.getOfficeSectionManagedObjectTeamTypes();
			IntFunction<String> teamLister = createCompareLister(eTeams, aTeams,
					(team) -> team.getOfficeSectionManagedObjectTeamName());
			TestCase.assertEquals("Incorrect number of teams for managed object source " + " (managed object="
					+ managedObjectName + ", managed object source=" + managedObjectSourceName + ", sub section="
					+ subSectionName + ")" + teamLister.apply(-1), eTeams.length, aTeams.length);
			for (int t = 0; t < eTeams.length; t++) {
				OfficeSectionManagedObjectTeamType eTeam = eTeams[t];
				OfficeSectionManagedObjectTeamType aTeam = aTeams[t];
				TestCase.assertEquals(
						"Incorrect name for team " + t + " (managed object=" + managedObjectName
								+ ", managed object source=" + managedObjectSourceName + ", sub section="
								+ subSectionName + ")" + teamLister.apply(t),
						eTeam.getOfficeSectionManagedObjectTeamName(), aTeam.getOfficeSectionManagedObjectTeamName());
			}
		}

		// Validate the sub sections
		OfficeSubSectionType[] eSubSections = eSection.getOfficeSubSectionTypes();
		OfficeSubSectionType[] aSubSections = aSection.getOfficeSubSectionTypes();
		TestCase.assertEquals("Incorect number of sub sections (sub section=" + subSectionName + ")",
				eSubSections.length, aSubSections.length);
		for (int i = 0; i < eSubSections.length; i++) {
			OfficeSubSectionType eSubSection = eSubSections[i];
			OfficeSubSectionType aSubSection = aSubSections[i];
			TestCase.assertEquals("Incorrect name for sub section " + i + " (sub section=" + subSectionName + ")",
					eSubSection.getOfficeSectionName(), aSubSection.getOfficeSectionName());
		}
	}

	/**
	 * Convenience method to load the {@link SectionType}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 */
	public static <S extends SectionSource> SectionType loadSectionType(Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Obtain the class loader
		ClassLoader classLoader = sectionSourceClass.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader);

		try {
			// Load and return the section type
			return loadSectionType(sectionSourceClass, sectionLocation, configurationContext, classLoader,
					propertyNameValuePairs);

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
	 * @param <S>
	 *            {@link SectionSource} type.
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
	public static <S extends SectionSource> SectionType loadSectionType(Class<S> sectionSourceClass,
			String sectionLocation, ConfigurationContext configurationContext, ClassLoader classLoader,
			String... propertyNameValuePairs) throws Exception {

		// Load and return the section type
		return getOfficeFloorCompiler().getSectionLoader().loadSectionType(sectionSourceClass, sectionLocation,
				new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Convenience method to load the {@link OfficeSectionType}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs
	 *            Listing of {@link Property} name/value pairs.
	 * @return {@link OfficeSectionType}.
	 */
	public static <S extends SectionSource> OfficeSectionType loadOfficeSectionType(String sectionName,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

		// Obtain the class loader
		ClassLoader classLoader = sectionSourceClass.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader);

		// Load and return the office section
		return loadOfficeSection(sectionName, sectionSourceClass, sectionLocation, configurationContext, classLoader,
				propertyNameValuePairs);
	}

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
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
	 * @return {@link OfficeSectionType}.
	 */
	public static <S extends SectionSource> OfficeSectionType loadOfficeSection(String sectionName,
			Class<S> sectionSourceClass, String sectionLocation, ConfigurationContext configurationContext,
			ClassLoader classLoader, String... propertyNameValuePairs) {

		// Load and return the office section
		return getOfficeFloorCompiler().getSectionLoader().loadOfficeSectionType(sectionName, sectionSourceClass,
				sectionLocation, new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler() {
		// Create the office floor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		return compiler;
	}

	/**
	 * Creates a compare listing of the items.
	 * 
	 * @param expectedItems
	 *            Expected items.
	 * @param actualItems
	 *            Actual items.
	 * @param valueExtractor
	 *            Extracts the value from the item.
	 * @return {@link IntFunction} to generate the compare list highlighting the
	 *         items at the particular index.
	 */
	private static <O> IntFunction<String> createCompareLister(O[] expectedItems, O[] actualItems,
			Function<O, String> valueExtractor) {
		return (index) -> {
			return "\n\nExpected:\n    " + createLister(expectedItems, valueExtractor).apply(index)
					+ "\n\nActual:\n    " + createLister(actualItems, valueExtractor).apply(index) + "\n\n";
		};
	}

	/**
	 * Generates a list from the items.
	 * 
	 * @param items
	 *            Items to generate list.
	 * @param valueExtractor
	 *            Extracts the value from the item.
	 * @return {@link IntFunction} to generate the list highlighting the items
	 *         at the particular index.
	 */
	private static <O> IntFunction<String> createLister(O[] items, Function<O, String> valueExtractor) {
		return (index) -> {
			StringBuilder list = new StringBuilder();
			boolean isFirst = true;
			for (int i = 0; i < items.length; i++) {
				if (!isFirst) {
					list.append(", ");
				}
				isFirst = false;
				if (i == index) {
					list.append("[");
				}
				list.append(valueExtractor.apply(items[i]));
				if (i == index) {
					list.append("]");
				}
			}
			return list.toString();
		};
	}

	/**
	 * All access via static methods.
	 */
	private SectionLoaderUtil() {
	}

}