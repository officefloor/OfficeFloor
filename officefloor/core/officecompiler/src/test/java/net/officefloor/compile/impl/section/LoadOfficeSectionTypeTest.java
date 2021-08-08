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

package net.officefloor.compile.impl.section;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.impl.structure.FunctionObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.DependencyLabeller;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Tests loading the {@link OfficeSectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeSectionTypeTest extends AbstractStructureTestCase {

	/**
	 * Ensure can load an empty {@link OfficeSectionType}.
	 */
	public void testLoadEmptySection() {

		// Load the empty office section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			// Leave empty
		});

		// Ensure empty
		assertEquals("Incorrect section name", "SECTION", section.getOfficeSectionName());
		assertEquals("Should be no sub section", 0, section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no functions", 0, section.getOfficeFunctionTypes().length);
	}

	/**
	 * Ensure can load a {@link SubSectionType}.
	 */
	public void testLoadSubSection() {

		// Load the office section with a sub section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.addSubSection("SUB_SECTION", null);
		});

		// Validate results
		assertEquals("Should have a sub section", 1, section.getOfficeSubSectionTypes().length);
		OfficeSubSectionType subSection = section.getOfficeSubSectionTypes()[0];
		assertEquals("Incorrect sub section", "SUB_SECTION", subSection.getOfficeSectionName());
		assertSame("Incorrect parent section", section, subSection.getParentOfficeSubSectionType());
		assertEquals("Should be no sub section functions", 0, subSection.getOfficeFunctionTypes().length);
		assertEquals("Should be no functions", 0, section.getOfficeFunctionTypes().length);
	}

	/**
	 * Ensure can load a sub {@link SubSectionType}. To ensure recursive loading of
	 * the {@link SubSectionType} instances.
	 */
	public void testLoadSubSubSection() {

		// Load the office section with a sub sub section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.addSubSection("SUB_SECTION", new SectionMaker() {
				@Override
				public void make(SectionMakerContext context) {
					context.addSubSection("SUB_SUB_SECTION", null);
				}
			});
		});

		// Validate the results
		assertEquals("Should have a sub section", 1, section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no functions on section", 0, section.getOfficeFunctionTypes().length);
		OfficeSubSectionType subSection = section.getOfficeSubSectionTypes()[0];
		assertEquals("Should have a sub sub section", 1, subSection.getOfficeSubSectionTypes().length);
		assertSame("Incorrect parent of sub section", section, subSection.getParentOfficeSubSectionType());
		assertEquals("Should be no functions on sub section", 0, subSection.getOfficeFunctionTypes().length);
		OfficeSubSectionType subSubSection = subSection.getOfficeSubSectionTypes()[0];
		assertEquals("Incorrect sub sub section", "SUB_SUB_SECTION", subSubSection.getOfficeSectionName());
		assertSame("Incorrect parent of sub sub section", subSection, subSubSection.getParentOfficeSubSectionType());
		assertEquals("Should be no functions on sub sub section", 0, subSubSection.getOfficeFunctionTypes().length);
	}

	/**
	 * Ensure can load a {@link SectionFunctionType}.
	 */
	public void testLoadSectionFunction() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the office section with a section function
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.addFunction("NAMESPACE", "FUNCTION", functionFactory, null);
		});

		// Validate results
		assertEquals("Should be no sub sections", 0, section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no managed object s", 0, section.getOfficeSectionManagedObjectTypes().length);
		assertEquals("Should have a single function", 1, section.getOfficeFunctionTypes().length);
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect function name", "FUNCTION", function.getOfficeFunctionName());
		assertSame("Incorrect parent section for function", section, function.getOfficeSubSectionType());
	}

	/**
	 * Ensure no {@link DependentObjectType} if {@link ManagedFunctionObjectType}
	 * not linked.
	 */
	public void testFunctionObjectDependencyNotLinked() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record not linked on first attempt to retrieve dependent
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "SECTION", "FUNCTION", "QUALIFIED-OBJECT"),
				FunctionObjectNodeImpl.class,
				"Function Object QUALIFIED-OBJECT is not linked to a DependentObjectNode");
		// Note: does not make it to UNQUALIFIED-OBJECT

		// Replay
		this.replayMockObjects();

		// Load the function object dependency not linked
		OfficeSectionType section = this.loadOfficeSectionType(false, "SECTION", (context) -> {
			SectionFunction function = context.addFunction("NAMESPACE", "FUNCTION", functionFactory,
					new FunctionMaker() {
						@Override
						public void make(FunctionMakerContext maker) {
							maker.addObject("UNQUALIFIED-OBJECT", Connection.class, null);
							maker.addObject("QUALIFIED-OBJECT", String.class, "QUALIFIED");
						}
					});
			function.getFunctionObject("UNQUALIFIED-OBJECT");
			function.getFunctionObject("QUALIFIED-OBJECT");
		});
		assertNull("Should not load section", section);

		// Verify the mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionInputType}.
	 */
	public void testLoadOfficeSectionInput() {

		// Load the office section with an office section input
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.getBuilder().addSectionInput("INPUT", String.class.getName());
		});

		// Validate results
		assertEquals("Should have office section input", 1, section.getOfficeSectionInputTypes().length);
		OfficeSectionInputType input = section.getOfficeSectionInputTypes()[0];
		assertEquals("Incorrect office section input", "INPUT", input.getOfficeSectionInputName());
		assertEquals("Incorrect office section input parameter type", String.class.getName(), input.getParameterType());
		assertEquals("Should be no office section outputs", 0, section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have no office section objects", 0, section.getOfficeSectionObjectTypes().length);
	}

	/**
	 * Ensure can create input with annotations.
	 */
	public void testInputWithAnnotations() {

		// Load office section type
		OfficeSectionType type = this.loadOfficeSectionType("SECTION", (context) -> {
			context.getBuilder().addSectionInput("NO_ANNOTATION", null);
			context.getBuilder().addSectionInput("ANNOTATION", null).addAnnotation("TEST");
		});

		// Ensure correct input with annotation
		assertEquals("Incorrect number of inputs", 2, type.getOfficeSectionInputTypes().length);
		OfficeSectionInputType annotation = type.getOfficeSectionInputTypes()[0];
		assertEquals("Incorrect input", "ANNOTATION", annotation.getOfficeSectionInputName());
		assertEquals("Incorrect number of annotations", 1, annotation.getAnnotations().length);
		assertEquals("Incorrect annotations", "TEST", annotation.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", "TEST", annotation.getAnnotation(String.class));
		OfficeSectionInputType noAnnotation = type.getOfficeSectionInputTypes()[1];
		assertEquals("Should be no annotations", 0, noAnnotation.getAnnotations().length);
		assertNull("Should be no by type annotation", noAnnotation.getAnnotation(String.class));
	}

	/**
	 * Ensure can load a {@link OfficeSectionOutput}.
	 */
	public void testLoadOfficeSectionOutput() {

		// Load the office section with an office section output
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.getBuilder().addSectionOutput("OUTPUT", Exception.class.getName(), true);
		});

		// Validate results
		assertEquals("Should be no office section inputs", 0, section.getOfficeSectionInputTypes().length);
		assertEquals("Should have office section output", 1, section.getOfficeSectionOutputTypes().length);
		OfficeSectionOutputType output = section.getOfficeSectionOutputTypes()[0];
		assertEquals("Incorrect office section output", "OUTPUT", output.getOfficeSectionOutputName());
		assertEquals("Incorrect office section output argument type", Exception.class.getName(),
				output.getArgumentType());
		assertTrue("Incorrect office section output escalation only flag", output.isEscalationOnly());
		assertEquals("Should have no office section objects", 0, section.getOfficeSectionObjectTypes().length);
	}

	/**
	 * Ensure can create output with annotations.
	 */
	public void testOutputWithAnnotations() {

		// Load office section type
		OfficeSectionType type = this.loadOfficeSectionType("SECTION", (context) -> {
			context.getBuilder().addSectionOutput("NO_ANNOTATION", null, false);
			context.getBuilder().addSectionOutput("ANNOTATION", null, false).addAnnotation("TEST");
		});

		// Ensure correct output with annotation
		assertEquals("Incorrect number of outputs", 2, type.getOfficeSectionOutputTypes().length);
		OfficeSectionOutputType annotation = type.getOfficeSectionOutputTypes()[0];
		assertEquals("Incorrect input", "ANNOTATION", annotation.getOfficeSectionOutputName());
		assertEquals("Incorrect number of annotations", 1, annotation.getAnnotations().length);
		assertEquals("Incorrect annotations", "TEST", annotation.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", "TEST", annotation.getAnnotation(String.class));
		OfficeSectionOutputType noAnnotation = type.getOfficeSectionOutputTypes()[1];
		assertEquals("Should be no annotations", 0, noAnnotation.getAnnotations().length);
		assertNull("Should be no by type annotation", noAnnotation.getAnnotation(String.class));
	}

	/**
	 * Ensure can add an {@link OfficeSectionObject}.
	 */
	public void testLoadOfficeSectionObject() {

		// Load the office section with an office section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			context.getBuilder().addSectionObject("OBJECT", Connection.class.getName());
		});

		// Validate results
		assertEquals("Should be no office section inputs", 0, section.getOfficeSectionInputTypes().length);
		assertEquals("Should be no office section outputs", 0, section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have office section object", 1, section.getOfficeSectionObjectTypes().length);
		OfficeSectionObjectType object = section.getOfficeSectionObjectTypes()[0];
		assertEquals("Incorrect office section object", "OBJECT", object.getOfficeSectionObjectName());
		assertEquals("Incorrect office section object, object type", Connection.class.getName(),
				object.getObjectType());
		assertNull("Office section object should not be qualified", object.getTypeQualifier());
	}

	/**
	 * Ensure can add a qualified {@link OfficeSectionObject}.
	 */
	public void testLoadQualifiedOfficeSectionObject() {

		// Load the office section with an office section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			SectionObject object = context.getBuilder().addSectionObject("OBJECT", Connection.class.getName());
			object.setTypeQualifier("QUALIFIED");
		});

		// Validate results
		assertEquals("Should be no office section inputs", 0, section.getOfficeSectionInputTypes().length);
		assertEquals("Should be no office section outputs", 0, section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have office section object", 1, section.getOfficeSectionObjectTypes().length);
		OfficeSectionObjectType object = section.getOfficeSectionObjectTypes()[0];
		assertEquals("Incorrect office section object", "OBJECT", object.getOfficeSectionObjectName());
		assertEquals("Incorrect office section object, object type", Connection.class.getName(),
				object.getObjectType());
		assertEquals("Incorrect office section object, type qualifier", "QUALIFIED", object.getTypeQualifier());
	}

	/**
	 * Ensure can load a {@link OfficeSectionManagedObject}.
	 */
	public void testLoadSectionManagedObject() {

		// Load the section managed object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			SectionManagedObjectSource source = context.addManagedObjectSource("MO_SOURCE", null);
			source.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);
		});

		// Validate the results
		assertEquals("Should have no sub section", 0, section.getOfficeSubSectionTypes().length);
		assertEquals("Should have no functions", 0, section.getOfficeFunctionTypes().length);
		assertEquals("Should have a section managed object", 1, section.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType mo = section.getOfficeSectionManagedObjectTypes()[0];
		assertEquals("Incorrect managed object name", "MO", mo.getOfficeSectionManagedObjectName());
		assertEquals("Should not have dependencies", 0, mo.getObjectDependencies().length);
		OfficeSectionManagedObjectSourceType moSource = mo.getOfficeSectionManagedObjectSourceType();
		assertNotNull("Should have a section managed object source", moSource);
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
	}

	/**
	 * Ensure no {@link DependentManagedObject} if {@link ManagedObjectDependency}
	 * not linked.
	 */
	public void testSectionManagedObjectDependencyNotLinked() {

		// Record not linked on first attempt to retrieve dependent
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "SECTION", "MO", "QUALIFIED-DEPENDENCY"),
				ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency QUALIFIED-DEPENDENCY is not linked to a DependentObjectNode");
		// Note: does not make it to UNQUALIFIED-DEPENDENCY

		// Load the section managed object with a dependency
		this.replayMockObjects();
		OfficeSectionType section = this.loadOfficeSectionType(false, "SECTION", (context) -> {
			SectionManagedObjectSource source = context.addManagedObjectSource("MO_SOURCE", new ManagedObjectMaker() {
				@Override
				public void make(ManagedObjectMakerContext context) {

					// Qualified dependency
					DependencyLabeller<Indexed> qualified = context.getContext().addDependency(Connection.class);
					qualified.setLabel("QUALIFIED-DEPENDENCY");
					qualified.setTypeQualifier("QUALIFIED");

					// Unqualified dependency
					context.getContext().addDependency(String.class).setLabel("UNQUALIFIED-DEPENDENCY");
				}
			});
			SectionManagedObject mo = source.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);
			mo.getSectionManagedObjectDependency("QUALIFIED-DEPENDENCY");
			mo.getSectionManagedObjectDependency("UNQUALIFIED-DEPENDENCY");
		});
		assertNull("Should not load section", section);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionManagedObject} that supports an
	 * extension interface.
	 */
	public void testLoadSectionManagedObjectSupportingAnExtensionInterface() {

		// Load the section managed object supporting an extension interface
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			SectionManagedObjectSource source = context.addManagedObjectSource("MO_SOURCE", new ManagedObjectMaker() {
				@Override
				public void make(ManagedObjectMakerContext context) {
					context.addExtensionInterface(XAResource.class);
				}
			});
			source.addSectionManagedObject("MO", ManagedObjectScope.FUNCTION);
		});

		// Validate the results
		assertEquals("Should have a section managed object", 1, section.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType mo = section.getOfficeSectionManagedObjectTypes()[0];
		assertEquals("Should have a supported extension interface", 1, mo.getSupportedExtensionInterfaces().length);
		Class<?> supportedEi = mo.getSupportedExtensionInterfaces()[0];
		assertEquals("Incorrect supported extension interface", XAResource.class, supportedEi);
		OfficeSectionManagedObjectSourceType moSource = mo.getOfficeSectionManagedObjectSourceType();
		assertNotNull("Should have a section managed object source", moSource);
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} linked to
	 * {@link SectionManagedObject} of same {@link OfficeSubSection}.
	 */
	public void testFunctionDependentOnManagedObjectOfSameSection() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on managed object of same section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {

			// Add the function object and managed object
			FunctionObject object = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory, "OBJECT",
					Connection.class, null);
			SectionManagedObjectSource moSource = context.addManagedObjectSource("MO_SOURCE", null);
			SectionManagedObject managedObject = moSource.addSectionManagedObject("MO", ManagedObjectScope.THREAD);

			// Link function object to managed object
			context.getBuilder().link(object, managedObject);
		});

		// Validate link to dependent managed object
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType dependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT", dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO", mo.getDependentObjectName());

		// Validate the default type qualification (from managed object type)
		assertEquals("Incorrect number of type qualifications", 1, mo.getTypeQualifications().length);
		TypeQualification qualification = mo.getTypeQualifications()[0];
		assertEquals("Incorrect dependent type", Object.class.getName(), qualification.getType());
		assertNull("Dependent should not be qualified for default auto-wire", qualification.getQualifier());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} provides qualification.
	 */
	public void testFunctionDependentOnQualifiedSectionManagedObject() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on managed object of same section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {

			// Add the function object and managed object
			FunctionObject object = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory, "OBJECT",
					Connection.class, null);
			SectionManagedObjectSource moSource = context.addManagedObjectSource("MO_SOURCE", null);
			SectionManagedObject managedObject = moSource.addSectionManagedObject("MO", ManagedObjectScope.THREAD);

			// Add type qualification (allows distinguishing)
			managedObject.addTypeQualification("QUALIFIED", Integer.class.getName());
			managedObject.addTypeQualification(null, String.class.getName());

			// Link function object to managed object
			context.getBuilder().link(object, managedObject);
		});

		// Validate link to dependent managed object
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType dependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT", dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO", mo.getDependentObjectName());
		assertTrue("Incorrect managed object type: " + mo.getClass().getName(),
				mo instanceof OfficeSectionManagedObjectType);

		// Validate the specifying auto-wiring
		assertEquals("Incorrect number of type qualifiers", 2, mo.getTypeQualifications().length);
		TypeQualification one = mo.getTypeQualifications()[0];
		assertEquals("Incorrect first qualified type", Integer.class.getName(), one.getType());
		assertEquals("Incorrect first type qualifier", "QUALIFIED", one.getQualifier());
		TypeQualification two = mo.getTypeQualifications()[1];
		assertEquals("Incorrect second qualified type", String.class.getName(), two.getType());
		assertNull("Second type should not be qualified", two.getQualifier());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} linked to
	 * {@link SectionManagedObject} of another {@link OfficeSubSection}.
	 */
	public void testFunctionDependentOnManagedObjectOfAnotherSection() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on managed object of another
		// section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {

			// Add the section with function object
			SubSection objectSection = context.addSubSection("OBJECT_SECTION", new SectionMaker() {
				@Override
				public void make(SectionMakerContext context) {
					// Add the funtion object
					FunctionObject object = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory,
							"OBJECT", Connection.class, null);

					// Link function object to section output
					SectionObject sectionObject = context.getBuilder().addSectionObject("SECTION_OBJECT",
							Connection.class.getName());
					context.getBuilder().link(object, sectionObject);
				}
			});
			SubSectionObject subSectionObject = objectSection.getSubSectionObject("SECTION_OBJECT");

			// Add the managed object
			SectionManagedObjectSource moSource = context.addManagedObjectSource("MO_SOURCE", null);
			SectionManagedObject managedObject = moSource.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);

			// Link function object to managed object
			context.getBuilder().link(subSectionObject, managedObject);
		});

		// Validate link to dependent managed object
		OfficeFunctionType function = section.getOfficeSubSectionTypes()[0].getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType dependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT", dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO", mo.getDependentObjectName());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} for a
	 * {@link ManagedObjectDependency}.
	 */
	public void testManagedObjectDependentOnAnotherManagedObject() {

		// Load the section managed object with a dependency
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {

			// Add the first managed object (with dependency)
			SectionManagedObjectSource sourceOne = context.addManagedObjectSource("MO_SOURCE_ONE",
					new ManagedObjectMaker() {
						@Override
						public void make(ManagedObjectMakerContext context) {
							context.getContext().addDependency(Connection.class).setLabel("DEPENDENCY");
						}
					});
			SectionManagedObject moOne = sourceOne.addSectionManagedObject("MO_ONE", ManagedObjectScope.PROCESS);
			SectionManagedObjectDependency dependency = moOne.getSectionManagedObjectDependency("DEPENDENCY");

			// Add the second managed object (no dependency)
			SectionManagedObjectSource sourceTwo = context.addManagedObjectSource("MO_SOURCE_TWO", null);
			SectionManagedObject moTwo = sourceTwo.addSectionManagedObject("MO_TWO", ManagedObjectScope.PROCESS);

			// Link dependency to second managed object
			context.getBuilder().link(dependency, moTwo);
		});

		// Ensure correct number of managed object sources
		assertEquals("Should have a two managed objects", 2, section.getOfficeSectionManagedObjectTypes().length);

		// Obtain the managed objects
		OfficeSectionManagedObjectType moOne = section.getOfficeSectionManagedObjectTypes()[0];
		OfficeSectionManagedObjectType moTwo = section.getOfficeSectionManagedObjectTypes()[1];

		// Validate managed object one
		assertEquals("Incorrect managed object name", "MO_ONE", moOne.getOfficeSectionManagedObjectName());
		assertEquals("MO_ONE should have a dependency", 1, moOne.getObjectDependencies().length);
		ObjectDependencyType dependency = moOne.getObjectDependencies()[0];
		assertEquals("Incorrect dependency name", "DEPENDENCY", dependency.getObjectDependencyName());
		assertEquals("Incorrect dependency type", Connection.class.getName(), dependency.getObjectDependencyType());
		assertNotNull("Dependency should be linked", dependency.getDependentObjectType());
		OfficeSectionManagedObjectSourceType moSourceOne = moOne.getOfficeSectionManagedObjectSourceType();
		assertNotNull("MO_ONE should have a section managed object source", moSourceOne);
		assertEquals("Incorrect managed object source name", "MO_SOURCE_ONE",
				moSourceOne.getOfficeSectionManagedObjectSourceName());

		// Validate managed object two
		assertEquals("Incorrect managed object name", "MO_TWO", moTwo.getOfficeSectionManagedObjectName());
		assertEquals("MO_TWO should not have a dependency", 0, moTwo.getObjectDependencies().length);
		OfficeSectionManagedObjectSourceType moSourceTwo = moTwo.getOfficeSectionManagedObjectSourceType();
		assertNotNull("MO_TWO should have a section managed object source", moSourceTwo);
		assertEquals("Incorrect managed object source name", "MO_SOURCE_TWO",
				moSourceTwo.getOfficeSectionManagedObjectSourceName());

		// Ensure dependency is linked to correct managed object
		assertEquals("Incorrect dependent object", dependency.getDependentObjectType().getDependentObjectName(),
				moTwo.getDependentObjectName());
	}

	/**
	 * Ensure can get {@link FunctionObject} linked to {@link ManagedObject} which
	 * has a {@link ManagedObjectDependency} linked to another
	 * {@link ManagedObject}.
	 */
	public void testFunctionDependentOnManagedObjectDependentOnAnotherManagedObject() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on managed object of same section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {

			// Add the function object
			FunctionObject functionObject = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory,
					"OBJECT", Connection.class, null);

			// Add the first managed object with a dependency
			SectionManagedObjectSource moSourceOne = context.addManagedObjectSource("MO_SOURCE_ONE",
					new ManagedObjectMaker() {
						@Override
						public void make(ManagedObjectMakerContext context) {
							context.getContext().addDependency(DataSource.class).setLabel("DEPENDENCY");
						}
					});
			SectionManagedObject moOne = moSourceOne.addSectionManagedObject("MO_ONE", ManagedObjectScope.THREAD);
			SectionManagedObjectDependency dependency = moOne.getSectionManagedObjectDependency("DEPENDENCY");

			// Add the second managed object (no dependency)
			SectionManagedObjectSource sourceTwo = context.addManagedObjectSource("MO_SOURCE_TWO", null);
			SectionManagedObject moTwo = sourceTwo.addSectionManagedObject("MO_TWO", ManagedObjectScope.PROCESS);

			// Link function object to first managed object
			context.getBuilder().link(functionObject, moOne);

			// Link managed object dependency to managed object
			context.getBuilder().link(dependency, moTwo);
		});

		// Validate link to dependent managed object
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of function object dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType functionDependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect function object dependency", "OBJECT", functionDependency.getObjectDependencyName());
		assertEquals("Incorrect function object dependency type", Connection.class.getName(),
				functionDependency.getObjectDependencyType());
		DependentObjectType mo = functionDependency.getDependentObjectType();
		assertEquals("Incorrect function dependent managed object", "MO_ONE", mo.getDependentObjectName());
		assertEquals("Incorrect number of managed object dependencies", 1, mo.getObjectDependencies().length);
		ObjectDependencyType moDependency = mo.getObjectDependencies()[0];
		assertEquals("Incorrect managed object dependency", "DEPENDENCY", moDependency.getObjectDependencyName());
		assertEquals("Incorrect managed object dependency type", DataSource.class.getName(),
				moDependency.getObjectDependencyType());
		DependentObjectType dependentMo = moDependency.getDependentObjectType();
		assertEquals("Incorrect managed object dependent managed object", "MO_TWO",
				dependentMo.getDependentObjectName());
		assertEquals("Incorrect number of dependencies for managed object", 0,
				dependentMo.getObjectDependencies().length);
	}

	/**
	 * Ensure {@link FunctionObject} flagged as parameter does not provide a
	 * {@link DependentManagedObject}.
	 */
	public void testFunctionObjectAsParameter() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on managed object of same section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			// Add the function object as parameter
			FunctionObject functionObject = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory,
					"OBJECT", Connection.class, null);
			functionObject.flagAsParameter();
		});

		// Validate no dependent object
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of function object dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType functionDependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect function object dependency", "OBJECT", functionDependency.getObjectDependencyName());
		assertEquals("Incorrect function object dependency type", Connection.class.getName(),
				functionDependency.getObjectDependencyType());
		assertTrue("Should be a paramaeter", functionDependency.isParameter());

		// Ensure no dependent managed object for parameter
		assertNull("Should be no dependent for function parameter", functionDependency.getDependentObjectType());
	}

	/**
	 * Ensure {@link FunctionObject} not flagged as parameter but issue as not
	 * linked.
	 */
	public void testFunctionObjectNotParameterAndNotLinked() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Issue as not linked
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "SECTION", "FUNCTION", "OBJECT"),
				FunctionObjectNodeImpl.class, "Function Object OBJECT is not linked to a DependentObjectNode");

		// Load the function object dependent on section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			// Add the function object as parameter
			context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory, "OBJECT", Connection.class, null);
		});
		assertNull("Should not load section", section);
	}

	/**
	 * Ensure {@link FunctionObject} not flagged as parameter.
	 */
	public void testFunctionObjectNotParameterLinked() {

		final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Load the function object dependent on section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION", (context) -> {
			// Add the function object as parameter
			FunctionObject functionObject = context.addFunctionObject("NAMESPACE", "FUNCTION", functionFactory,
					"OBJECT", Connection.class, null);
			SectionObject sectionObject = context.getBuilder().addSectionObject("SECTION_OBJECT",
					Connection.class.getName());
			sectionObject.setTypeQualifier("QUALIFIER");
			context.getBuilder().link(functionObject, sectionObject);
		});

		// Validate dependent object
		OfficeFunctionType function = section.getOfficeFunctionTypes()[0];
		assertEquals("Incorrect number of function object dependencies", 1, function.getObjectDependencies().length);
		ObjectDependencyType functionDependency = function.getObjectDependencies()[0];
		assertEquals("Incorrect function object dependency", "OBJECT", functionDependency.getObjectDependencyName());
		assertEquals("Incorrect function object dependency type", Connection.class.getName(),
				functionDependency.getObjectDependencyType());
		assertFalse("Should not be a paramaeter", functionDependency.isParameter());

		// Ensure no dependent managed object for parameter
		DependentObjectType dependentObject = functionDependency.getDependentObjectType();
		assertNotNull("Should have dependent for object", dependentObject);
		assertEquals("Incorrect dependent object name", "SECTION_OBJECT", dependentObject.getDependentObjectName());
		assertEquals("Should not have dependent object dependencies", 0,
				dependentObject.getObjectDependencies().length);
		TypeQualification[] typeQualifications = dependentObject.getTypeQualifications();
		assertEquals("Incorrect number of dependent type qualifications", 1, typeQualifications.length);
		assertEquals("Incorrect dependent type", Connection.class.getName(), typeQualifications[0].getType());
		assertEquals("Incorrect dependent qualification", "QUALIFIER", typeQualifications[0].getQualifier());
	}

}
