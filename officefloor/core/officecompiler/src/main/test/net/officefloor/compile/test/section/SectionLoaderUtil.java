/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.test.section;

import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.CompileContextImpl;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.test.annotation.AnnotationLoaderUtil;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.compile.test.util.LoaderUtil;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;

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
	 * @param <S>                {@link SectionSource} type.
	 * @param sectionSourceClass {@link SectionSource} class.
	 * @param propertyNameLabels Listing of name/label pairs for the
	 *                           {@link Property} instances.
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
	 * Validates the {@link SectionSourceSpecification} for the
	 * {@link SectionSource}.
	 * 
	 * @param sectionSource      {@link SectionSource}.
	 * @param propertyNameLabels Listing of name/label pairs for the
	 *                           {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static PropertyList validateSpecification(SectionSource sectionSource, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler().getSectionLoader().loadSpecification(sectionSource);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Convenience method to obtain the class path location.
	 * 
	 * @param offsetClass  Class indicating the package that the resource is within.
	 *                     Typically this will be the {@link Assert} instance.
	 * @param resourceName Name of the resource.
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
		OfficeNode office = context.createOfficeNode("<office>", null);
		return context.createSectionNode(SectionLoaderUtil.class.getSimpleName(), office);
	}

	/**
	 * Creates the {@link SectionTypeBuilder}.
	 * 
	 * @return {@link SectionTypeBuilder}.
	 */
	public static SectionTypeBuilder createSectionTypeBuilder() {
		return new SectionTypeBuilderImpl(createSectionDesigner());
	}

	/**
	 * Builds the {@link SectionType} for the {@link SectionDesigner}.
	 * 
	 * @param designer {@link SectionDesigner}.
	 * @return {@link SectionType}.
	 */
	public static SectionType buildSectionType(SectionDesigner designer) {

		// Compile Context
		CompileContext compileContext = new CompileContextImpl(null);

		// Cast to obtain expected section type
		if (!(designer instanceof SectionNode)) {
			Assert.fail("designer must be created from createSectionDesigner");
		}
		return ((SectionNode) designer).loadSectionType(compileContext);
	}

	/**
	 * Facade method to validate the {@link SectionType} and {@link OfficeSection}.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param designer               {@link SectionDesigner} containing the expected
	 *                               {@link SectionType}/{@link OfficeSection}.
	 * @param sectionSourceClass     Class of the {@link SectionSource} being
	 *                               tested.
	 * @param offsetClass            Object indicating the package that the resource
	 *                               is within. Typically this will be the
	 *                               {@link Assert} instance.
	 * @param resourceName           Name of the resource. This is used with the
	 *                               <code>offsetObject</code> to determine the
	 *                               {@link OfficeSection} location.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
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
	 * @param <S>                    {@link SectionSource} type.
	 * @param designer               {@link SectionDesigner} containing the expected
	 *                               {@link SectionType}/{@link OfficeSection}.
	 * @param sectionSourceClass     Class of the {@link SectionSource} being
	 *                               tested.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
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
	 * @param designer               {@link SectionDesigner} containing the expected
	 *                               {@link SectionType}.
	 * @param sectionSource          {@link SectionSource} instance being tested.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 */
	public static void validateSectionType(SectionDesigner designer, SectionSource sectionSource,
			String sectionLocation, String... propertyNameValuePairs) {

		// Load the actual section type
		SectionType actualSection = loadSectionType(sectionSource, sectionLocation, propertyNameValuePairs);

		// Validate the section type
		validateSectionType(designer, actualSection);
	}

	/**
	 * Convenience method that validates the loaded {@link SectionType} against
	 * expected {@link SectionType} from the {@link SectionDesigner}.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param designer               {@link SectionDesigner} containing the expected
	 *                               {@link SectionType}.
	 * @param sectionSourceClass     Class of the {@link SectionSource} being
	 *                               tested.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateSectionType(SectionDesigner designer,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

		// Load the actual section type
		SectionType actualSection = loadSectionType(sectionSourceClass, sectionLocation, propertyNameValuePairs);

		// Validate the section type
		validateSectionType(designer, actualSection);
	}

	/**
	 * Validates the {@link SectionType}.
	 * 
	 * @param designer      {@link SectionDesigner} containing the expected
	 *                      {@link SectionType}.
	 * @param actualSection Actual {@link SectionType} to validate.
	 */
	public static void validateSectionType(SectionDesigner designer, SectionType actualSection) {

		// Build the expected section type
		SectionType expectedSection = buildSectionType(designer);

		// Validate section inputs are as expected
		SectionInputType[] eInputs = expectedSection.getSectionInputTypes();
		SectionInputType[] aInputs = actualSection.getSectionInputTypes();
		LoaderUtil.assertLength("Incorrect number of inputs", eInputs, aInputs, (input) -> input.getSectionInputName());
		for (int i = 0; i < eInputs.length; i++) {
			SectionInputType eInput = eInputs[i];
			SectionInputType aInput = aInputs[i];
			Assert.assertEquals("Incorrect name for input " + i, eInput.getSectionInputName(),
					aInput.getSectionInputName());
			Assert.assertEquals("Incorrect parameter type for input " + i, eInput.getParameterType(),
					aInput.getParameterType());

			// Validate the input annotations
			Object[] eAnnotations = eInput.getAnnotations();
			Object[] aAnnotations = aInput.getAnnotations();
			LoaderUtil.assertLength("Incorrect number of annoations for input " + eInput.getSectionInputName(),
					eAnnotations, aAnnotations, (annotation) -> annotation.toString());
			for (int a = 0; a < eAnnotations.length; a++) {
				Assert.assertEquals("Incorrect annotation " + a + " for input " + eInput.getSectionInputName(),
						eAnnotations[a].getClass(), aAnnotations[a].getClass());
			}
		}

		// Validate the section outputs are as expected
		SectionOutputType[] eOutputs = expectedSection.getSectionOutputTypes();
		SectionOutputType[] aOutputs = actualSection.getSectionOutputTypes();
		LoaderUtil.assertLength("Incorrect number of outputs", eOutputs, aOutputs,
				(output) -> output.getSectionOutputName());
		for (int i = 0; i < eOutputs.length; i++) {
			SectionOutputType eOutput = eOutputs[i];
			SectionOutputType aOutput = aOutputs[i];
			Assert.assertEquals("Incorrect name for output " + i, eOutput.getSectionOutputName(),
					aOutput.getSectionOutputName());
			Assert.assertEquals("Incorrect argument type for output " + i, eOutput.getArgumentType(),
					aOutput.getArgumentType());
			Assert.assertEquals("Incorrect escalation only for output " + i, eOutput.isEscalationOnly(),
					aOutput.isEscalationOnly());
		}

		// Validate the section objects are as expected
		SectionObjectType[] eObjects = expectedSection.getSectionObjectTypes();
		SectionObjectType[] aObjects = actualSection.getSectionObjectTypes();
		LoaderUtil.assertLength("Incorrect number of objects", eObjects, aObjects,
				(object) -> object.getSectionObjectName());
		for (int i = 0; i < eObjects.length; i++) {
			SectionObjectType eObject = eObjects[i];
			SectionObjectType aObject = aObjects[i];
			Assert.assertEquals("Incorrect name for object " + i, eObject.getSectionObjectName(),
					aObject.getSectionObjectName());
			String objectName = eObject.getSectionObjectName();
			Assert.assertEquals("Incorrect object type for object " + i + " (" + objectName + ")",
					eObject.getObjectType(), aObject.getObjectType());
			Assert.assertEquals("Incorrect type qualifier for object " + i + " (" + objectName + ")",
					eObject.getTypeQualifier(), aObject.getTypeQualifier());

			// Validate the annotations
			AnnotationLoaderUtil.validateAnnotations("for object " + i + "(" + objectName + ")",
					eObject.getAnnotations(), aObject.getAnnotations());
		}
	}

	/**
	 * Convenience method that validates the loaded {@link OfficeSection} against
	 * expected {@link OfficeSection} from the {@link SectionDesigner}.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param designer               {@link SectionDesigner} containing the expected
	 *                               {@link OfficeSection}.
	 * @param sectionSourceClass     Class of the {@link SectionSource} being
	 *                               tested.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 */
	public static <S extends SectionSource> void validateOfficeSection(SectionDesigner designer,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

		// Create the compile context
		CompileContext compileContext = new CompileContextImpl(null);

		// Cast to obtain expected section type
		if (!(designer instanceof SectionNode)) {
			Assert.fail("designer must be created from createSectionDesigner");
		}
		SectionNode section = (SectionNode) designer;
		section.initialise(sectionSourceClass.getName(), null, sectionLocation);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			section.addProperty(name, value);
		}
		OfficeSectionType eSection = section.loadOfficeSectionType(compileContext);

		// Load the actual section type
		OfficeSectionType aSection = loadOfficeSectionType(SectionLoaderUtil.class.getSimpleName(), sectionSourceClass,
				sectionLocation, propertyNameValuePairs);

		// Validate the office section
		Assert.assertEquals("Incorrect section name", eSection.getOfficeSectionName(), aSection.getOfficeSectionName());

		// Validate the office section inputs
		OfficeSectionInputType[] eInputs = eSection.getOfficeSectionInputTypes();
		OfficeSectionInputType[] aInputs = aSection.getOfficeSectionInputTypes();
		Assert.assertEquals("Incorrect number of section inputs", eInputs.length, aInputs.length);
		for (int i = 0; i < eInputs.length; i++) {
			OfficeSectionInputType eInput = eInputs[i];
			OfficeSectionInputType aInput = aInputs[i];
			Assert.assertEquals("Incorrect name for section input " + i, eInput.getOfficeSectionInputName(),
					aInput.getOfficeSectionInputName());
			Assert.assertEquals("Incorrect parameter type for section input " + i, eInput.getParameterType(),
					aInput.getParameterType());
		}

		// Validate the office section outputs
		OfficeSectionOutputType[] eOutputs = eSection.getOfficeSectionOutputTypes();
		OfficeSectionOutputType[] aOutputs = aSection.getOfficeSectionOutputTypes();
		Assert.assertEquals("Incorrect number of section outputs", eOutputs.length, aOutputs.length);
		for (int i = 0; i < eOutputs.length; i++) {
			OfficeSectionOutputType eOutput = eOutputs[i];
			OfficeSectionOutputType aOutput = aOutputs[i];
			Assert.assertEquals("Incorrect name for section output " + i, eOutput.getOfficeSectionOutputName(),
					aOutput.getOfficeSectionOutputName());
			Assert.assertEquals("Incorrect argument type for section output " + i, eOutput.getArgumentType(),
					aOutput.getArgumentType());
			Assert.assertEquals("Incorrect escalation only for section output " + i, eOutput.isEscalationOnly(),
					aOutput.isEscalationOnly());
		}

		// Validate the office section objects
		OfficeSectionObjectType[] eObjects = eSection.getOfficeSectionObjectTypes();
		OfficeSectionObjectType[] aObjects = aSection.getOfficeSectionObjectTypes();
		Assert.assertEquals("Incorrect number of section objects", eObjects.length, aObjects.length);
		for (int i = 0; i < eObjects.length; i++) {
			OfficeSectionObjectType eObject = eObjects[i];
			OfficeSectionObjectType aObject = aObjects[i];
			Assert.assertEquals("Incorrect name for section object " + i, eObject.getOfficeSectionObjectName(),
					aObject.getOfficeSectionObjectName());
			Assert.assertEquals("Incorrect object type for section object " + i, eObject.getObjectType(),
					aObject.getObjectType());
			Assert.assertEquals("Incorrect type qualifier for section object " + i, eObject.getTypeQualifier(),
					aObject.getTypeQualifier());
		}

		// Validate remaining of the office section
		validateOfficeSubSectionType(null, eSection, aSection);
	}

	/**
	 * Validates the {@link OfficeSubSection}.
	 * 
	 * @param subSectionName Name of the {@link OfficeSubSection} being validated.
	 * @param eSection       Expected {@link OfficeSubSection}.
	 * @param aSection       Actual {@link OfficeSubSection}.
	 */
	private static void validateOfficeSubSectionType(String subSectionName, OfficeSubSectionType eSection,
			OfficeSubSectionType aSection) {

		// Validate the office sub section
		Assert.assertEquals("Incorrect section name (parent section=" + subSectionName + ")",
				eSection.getOfficeSectionName(), aSection.getOfficeSectionName());

		// Determine this sub section name
		subSectionName = (subSectionName == null ? "" : subSectionName + ".") + eSection.getOfficeSectionName();

		// Validate the functions
		OfficeFunctionType[] eFunctions = eSection.getOfficeFunctionTypes();
		OfficeFunctionType[] aFunctions = aSection.getOfficeFunctionTypes();
		IntFunction<String> functionsLister = createCompareLister(eFunctions, aFunctions,
				(functionType) -> functionType.getOfficeFunctionName());
		Assert.assertEquals(
				"Incorrect number of functions (section=" + subSectionName + ")" + functionsLister.apply(-1),
				eFunctions.length, aFunctions.length);
		for (int i = 0; i < eFunctions.length; i++) {
			OfficeFunctionType eFunction = eFunctions[i];
			OfficeFunctionType aFunction = aFunctions[i];
			Assert.assertEquals(
					"Incorrect name for function " + i + " (sub section=" + subSectionName + ")"
							+ functionsLister.apply(i),
					eFunction.getOfficeFunctionName(), aFunction.getOfficeFunctionName());

			// Validate the dependencies
			ObjectDependencyType[] eDependencies = eFunction.getObjectDependencies();
			ObjectDependencyType[] aDependencies = aFunction.getObjectDependencies();
			IntFunction<String> dependencyLIster = createCompareLister(eDependencies, aDependencies,
					(dependency) -> dependency.getObjectDependencyName());
			Assert.assertEquals(
					"Incorrect number of dependencies for function " + i + " (sub section=" + subSectionName
							+ ", function=" + eFunction.getOfficeFunctionName() + ")" + dependencyLIster.apply(-1),
					eDependencies.length, aDependencies.length);
			for (int j = 0; j < eDependencies.length; j++) {
				ObjectDependencyType eDependency = eDependencies[j];
				ObjectDependencyType aDependency = aDependencies[j];
				Assert.assertEquals(
						"Incorrect name for dependency " + j + " (sub section=" + subSectionName + ", function="
								+ eFunction.getOfficeFunctionName() + ")" + dependencyLIster.apply(j),
						eDependency.getObjectDependencyName(), aDependency.getObjectDependencyName());
				// Do not check dependent as requires linking
			}
		}

		// Validate the managed objects
		OfficeSectionManagedObjectType[] eMos = eSection.getOfficeSectionManagedObjectTypes();
		OfficeSectionManagedObjectType[] aMos = aSection.getOfficeSectionManagedObjectTypes();
		IntFunction<String> mosLister = createCompareLister(eMos, aMos,
				(mos) -> mos.getOfficeSectionManagedObjectName());
		Assert.assertEquals(
				"Incorrect number of managed objects (sub section=" + subSectionName + ")" + mosLister.apply(-1),
				eMos.length, aMos.length);
		for (int j = 0; j < eMos.length; j++) {
			OfficeSectionManagedObjectType eMo = eMos[j];
			OfficeSectionManagedObjectType aMo = aMos[j];
			Assert.assertEquals("Incorrect name for managed object " + j + " (sub section=" + subSectionName + ")",
					eMo.getOfficeSectionManagedObjectName(), aMo.getOfficeSectionManagedObjectName());
			Assert.assertEquals(
					"Incorrect dependent name for managed object " + j + " (sub section=" + subSectionName + ")",
					eMo.getDependentObjectName(), aMo.getDependentObjectName());
			String managedObjectName = eMo.getOfficeSectionManagedObjectName();

			// Validate the managed object type qualifiers
			TypeQualification[] eTqs = eMo.getTypeQualifications();
			TypeQualification[] aTqs = aMo.getTypeQualifications();
			IntFunction<String> tqLister = createCompareLister(eTqs, aTqs,
					(tq) -> tq.getQualifier() + ":" + tq.getType());
			Assert.assertEquals("Incorrect number of type qualifiers for managed object " + j + " (sub section="
					+ subSectionName + ")" + tqLister.apply(-1), eTqs.length, aTqs.length);
			for (int q = 0; q < eTqs.length; q++) {
				TypeQualification eTq = eTqs[q];
				TypeQualification aTq = aTqs[q];
				Assert.assertEquals(
						"Incorrect qualifying qualifier " + q + " (managed object=" + managedObjectName
								+ ", sub section=" + subSectionName + ")" + tqLister.apply(q),
						eTq.getQualifier(), aTq.getQualifier());
				Assert.assertEquals("Incorrect qualifying type " + q + " (managed object=" + managedObjectName
						+ ", sub section=" + subSectionName + ")" + tqLister.apply(q), eTq.getType(), aTq.getType());
			}

			// Validate the managed object supported extension interfaces
			Class<?>[] eEis = eMo.getSupportedExtensionInterfaces();
			Class<?>[] aEis = aMo.getSupportedExtensionInterfaces();
			IntFunction<String> eiLister = createCompareLister(eEis, aEis, (ei) -> ei.getName());
			Assert.assertEquals("Incorrect number of supported extension interfaces for managed object " + j
					+ " (sub section=" + subSectionName + ")" + eiLister.apply(-1), eEis.length, aEis.length);
			for (int k = 0; k < eEis.length; k++) {
				Assert.assertEquals("Incorrect class for extension interface " + k + " (managed object="
						+ managedObjectName + ", sub section=" + subSectionName + ")" + eiLister.apply(k), eEis[k],
						aEis[k]);
			}

			// Validate the managed object source
			OfficeSectionManagedObjectSourceType eMoSource = eMo.getOfficeSectionManagedObjectSourceType();
			OfficeSectionManagedObjectSourceType aMoSource = aMo.getOfficeSectionManagedObjectSourceType();
			Assert.assertEquals(
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
			Assert.assertEquals("Incorrect number of teams for managed object source " + " (managed object="
					+ managedObjectName + ", managed object source=" + managedObjectSourceName + ", sub section="
					+ subSectionName + ")" + teamLister.apply(-1), eTeams.length, aTeams.length);
			for (int t = 0; t < eTeams.length; t++) {
				OfficeSectionManagedObjectTeamType eTeam = eTeams[t];
				OfficeSectionManagedObjectTeamType aTeam = aTeams[t];
				Assert.assertEquals(
						"Incorrect name for team " + t + " (managed object=" + managedObjectName
								+ ", managed object source=" + managedObjectSourceName + ", sub section="
								+ subSectionName + ")" + teamLister.apply(t),
						eTeam.getOfficeSectionManagedObjectTeamName(), aTeam.getOfficeSectionManagedObjectTeamName());
			}
		}

		// Validate the sub sections
		OfficeSubSectionType[] eSubSections = eSection.getOfficeSubSectionTypes();
		OfficeSubSectionType[] aSubSections = aSection.getOfficeSubSectionTypes();
		Assert.assertEquals("Incorect number of sub sections (sub section=" + subSectionName + ")", eSubSections.length,
				aSubSections.length);
		for (int i = 0; i < eSubSections.length; i++) {
			OfficeSubSectionType eSubSection = eSubSections[i];
			OfficeSubSectionType aSubSection = aSubSections[i];
			Assert.assertEquals("Incorrect name for sub section " + i + " (sub section=" + subSectionName + ")",
					eSubSection.getOfficeSectionName(), aSubSection.getOfficeSectionName());
		}
	}

	/**
	 * Convenience method to load the {@link SectionType}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param sectionSourceClass     Class of the {@link SectionSource}.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 */
	public static <S extends SectionSource> SectionType loadSectionType(Class<S> sectionSourceClass,
			String sectionLocation, String... propertyNameValuePairs) {

		// Load and return the section type
		return getOfficeFloorCompiler().getSectionLoader().loadSectionType(sectionSourceClass, sectionLocation,
				new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Convenience method to load the {@link SectionType}.
	 * 
	 * @param sectionSource          {@link SectionSource} instance.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 * @return {@link SectionType}.
	 */
	public static SectionType loadSectionType(SectionSource sectionSource, String sectionLocation,
			String... propertyNameValuePairs) {

		// Load and return the section type
		return getOfficeFloorCompiler().getSectionLoader().loadSectionType(sectionSource, sectionLocation,
				new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Convenience method to load the {@link OfficeSectionType}. It uses the
	 * {@link SectionSource} class's {@link ClassLoader} and subsequent
	 * {@link ClassLoaderConfigurationContext}.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param sectionName            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass     Class of the {@link SectionSource}.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @param propertyNameValuePairs Listing of {@link Property} name/value pairs.
	 * @return {@link OfficeSectionType}.
	 */
	public static <S extends SectionSource> OfficeSectionType loadOfficeSectionType(String sectionName,
			Class<S> sectionSourceClass, String sectionLocation, String... propertyNameValuePairs) {

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
		// Create the OfficeFloor compiler that fails on first issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		compiler.addSourceAliases();
		return compiler;
	}

	/**
	 * Creates a compare listing of the items.
	 * 
	 * @param expectedItems  Expected items.
	 * @param actualItems    Actual items.
	 * @param valueExtractor Extracts the value from the item.
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
	 * @param items          Items to generate list.
	 * @param valueExtractor Extracts the value from the item.
	 * @return {@link IntFunction} to generate the list highlighting the items at
	 *         the particular index.
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

	/**
	 * {@link SectionTypeBuilder} implementation.
	 */
	private static class SectionTypeBuilderImpl implements SectionTypeBuilder {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner designer;

		/**
		 * Instantiate.
		 * 
		 * @param designer {@link SectionDesigner}.
		 */
		private SectionTypeBuilderImpl(SectionDesigner designer) {
			this.designer = designer;
		}

		/*
		 * ===================== SectionTypeBuilder =====================
		 */

		@Override
		public void addSectionInput(String name, Class<?> parameterType) {
			this.designer.addSectionInput(name, parameterType == null ? null : parameterType.getName());
		}

		@Override
		public void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly) {
			this.designer.addSectionOutput(name, argumentType == null ? null : argumentType.getName(),
					isEscalationOnly);
		}

		@Override
		public void addSectionOutput(String name, Class<?> argumentType) {
			this.addSectionOutput(name, argumentType, false);
		}

		@Override
		public void addSectionEscalation(Class<?> escalationType) {
			this.addSectionOutput(escalationType.getName(), escalationType, true);
		}

		@Override
		public void addSectionObject(String name, Class<?> objectType, String typeQualifier,
				Class<?>... annotationTypes) {
			SectionObject sectionObject = this.designer.addSectionObject(name, objectType.getName());
			sectionObject.setTypeQualifier(typeQualifier);
			for (Class<?> annotationType : annotationTypes) {
				sectionObject.addAnnotation(annotationType);
			}
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return this.designer;
		}
	}

}
