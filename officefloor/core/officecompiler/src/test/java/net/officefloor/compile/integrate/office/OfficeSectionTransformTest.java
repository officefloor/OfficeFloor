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

package net.officefloor.compile.integrate.office;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionTransformer;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.ClassSectionSource.SectionClassManagedFunctionSource;
import net.officefloor.plugin.section.transform.TransformSectionDesigner;
import net.officefloor.plugin.section.transform.TransformSectionSource;

/**
 * Tests the {@link TransformSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionTransformTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(TransformSectionSource.class,
				TransformSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME, "Section Source");
	}

	/**
	 * Ensure correctly construct the expected type.
	 */
	public void testWrappedTypeValid() {

		// Create the type
		SectionDesigner type = this.createExpectedType(false, "");

		// Validate type is correct
		SectionLoaderUtil.validateSection(type, ClassSectionSource.class, MockSectionTypeClass.class.getName());
	}

	/**
	 * Ensure correct generation of default transformed type.
	 */
	public void testDefaultTransformedType() {

		// Create the type
		SectionDesigner type = this.createExpectedType(true, "");

		// Validate wrap section
		SectionLoaderUtil.validateSection(type, TransformSectionSource.class, (String) null,
				TransformSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME, ClassSectionSource.class.getName(),
				TransformSectionSource.PROPERTY_SECTION_LOCATION, MockSectionTypeClass.class.getName());
	}

	/**
	 * Ensure correct generation of transformed type.
	 */
	public void testTransformedType() {

		// Create the type
		SectionDesigner type = this.createExpectedType(true, MockTransformSectionSource.MOCK_PREFIX);

		// Validate wrap section
		SectionLoaderUtil.validateSection(type, MockTransformSectionSource.class, (String) null,
				TransformSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME, ClassSectionSource.class.getName(),
				TransformSectionSource.PROPERTY_SECTION_LOCATION, MockSectionTypeClass.class.getName(),
				TransformSectionSource.PROPERTY_SECTION_PROPERTY_PREFIX + "one", "A",
				TransformSectionSource.PROPERTY_SECTION_PROPERTY_PREFIX + "two", "B", "ignore", "C");
	}

	/**
	 * Ensure can provide {@link OfficeSectionTransformer} functionality.
	 */
	public void testOfficeSectionTransformation() throws Exception {

		// Create the OfficeFloor
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Add the transformer
			extender.addOfficeSectionTransformer(new MockTransformSectionSource());

			// Add first section
			OfficeSection one = extender.addOfficeSection("ONE", ClassSectionSource.class.getName(),
					MockSectionRunClass.class.getName());
			one.addProperty("one", "A");
			one.addProperty("two", "B");

			// Add section section
			OfficeSection two = extender.addOfficeSection("TWO", ClassSectionSource.class.getName(),
					MockSectionRunClass.class.getName());
			two.addProperty("one", "A");
			two.addProperty("two", "B");

			// Link via enhance inputs
			extender.link(one.getOfficeSectionOutput("MOCK_next"), two.getOfficeSectionInput("ENHANCED_INPUT"));
			extender.link(two.getOfficeSectionOutput("MOCK_next"), one.getOfficeSectionInput("ENHANCED_INPUT"));
		});

		try {

			// Trigger function to ensure linking via enhancements
			final int NUMBER_OF_VALUES = 2;
			List<String> values = new ArrayList<String>(NUMBER_OF_VALUES);
			officeFloor.getOffice("OFFICE").getFunctionManager("ONE.MOCK_TRANSFORMED.inputTwo").invokeProcess(values,
					null);

			// Ensure appropriate functions triggered
			assertEquals("Incorrect number of values", NUMBER_OF_VALUES, values.size());
			for (int i = 0; i < NUMBER_OF_VALUES; i++) {
				assertEquals("Incorrect value", String.valueOf(i), values.get(i));
			}

		} finally {
			// Ensure close OfficeFloor
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can provide multiple {@link OfficeSectionTransformer} instances
	 * chained together.
	 */
	public void testChainedOfficeSectionTransformation() throws Exception {

		// Create the OfficeFloor
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Add the multiple transformer
			extender.addOfficeSectionTransformer(new MockTransformSectionSource());
			extender.addOfficeSectionTransformer(new MockTransformSectionSource());

			// Add first section
			OfficeSection one = extender.addOfficeSection("ONE", ClassSectionSource.class.getName(),
					MockSectionRunClass.class.getName());
			one.addProperty("one", "A");
			one.addProperty("two", "B");

			// Add section section
			OfficeSection two = extender.addOfficeSection("TWO", ClassSectionSource.class.getName(),
					MockSectionRunClass.class.getName());
			two.addProperty("one", "A");
			two.addProperty("two", "B");

			// Link via enhance inputs
			extender.link(one.getOfficeSectionOutput("MOCK_MOCK_next"),
					two.getOfficeSectionInput("MOCK_ENHANCED_INPUT"));
			extender.link(two.getOfficeSectionOutput("MOCK_MOCK_next"),
					one.getOfficeSectionInput("MOCK_ENHANCED_INPUT"));
		});

		try {

			// Trigger function to ensure linking via enhancements
			final int NUMBER_OF_VALUES = 2;
			List<String> values = new ArrayList<String>(NUMBER_OF_VALUES);
			officeFloor.getOffice("OFFICE").getFunctionManager("ONE.MOCK_TRANSFORMED.MOCK_TRANSFORMED.inputTwo")
					.invokeProcess(values, null);

			// Ensure appropriate functions triggered
			assertEquals("Incorrect number of values", NUMBER_OF_VALUES, values.size());
			for (int i = 0; i < NUMBER_OF_VALUES; i++) {
				assertEquals("Incorrect value", String.valueOf(i), values.get(i));
			}

		} finally {
			// Ensure close OfficeFloor
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link TransformSectionSource}.
	 */
	public static class MockTransformSectionSource extends TransformSectionSource {

		/**
		 * Prefix on transformed {@link SectionSource}.
		 */
		public static final String MOCK_PREFIX = "MOCK_";

		/**
		 * Name of the transformed {@link SubSection}.
		 */
		private static final String MOCK_SECTION_NAME = MOCK_PREFIX + SUB_SECTION_NAME;

		/*
		 * ================= TransformSectionSource ================
		 */

		@Override
		protected void loadSubSection(String sectionSourceClassName, String sectionLocation, PropertyList properties)
				throws Exception {

			// Create the sub section
			SubSection subSection = this.getDesginer().addSubSection(MOCK_SECTION_NAME, sectionSourceClassName,
					sectionLocation);

			// Load the properties
			for (Property property : properties) {
				subSection.addProperty(property.getName(), property.getValue());
			}

			// Validate the properties
			Properties nameValues = properties.getProperties();
			switch (nameValues.size()) {
			case 4:
				// Validate the sub section properties
				assertEquals("Incorrect property one", "A",
						nameValues.get(TransformSectionSource.PROPERTY_SECTION_PROPERTY_PREFIX + "one"));
				assertEquals("Incorrect property two", "B",
						nameValues.get(TransformSectionSource.PROPERTY_SECTION_PROPERTY_PREFIX + "two"));
				assertEquals("Incorrect section source", ClassSectionSource.class.getName(),
						nameValues.get(TransformSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME));
				assertEquals("Incorrect section location", MockSectionRunClass.class.getName(),
						nameValues.get(TransformSectionSource.PROPERTY_SECTION_LOCATION));
				break;
			case 2:
				// Validate the section properties
				assertEquals("Incorrect property one", "A", nameValues.get("one"));
				assertEquals("Incorrect property two", "B", nameValues.get("two"));
				break;

			default:
				fail("Incorrect number of properties " + nameValues.size());
			}
		}

		@Override
		protected void loadSectionInput(SectionInputType inputType) throws Exception {

			// Obtain the designer
			TransformSectionDesigner designer = this.getDesginer();

			// Obtain the sub section
			SubSection subSection = designer.getSubSection(MOCK_SECTION_NAME);

			// Default map to Section Input
			String inputName = inputType.getSectionInputName();
			SubSectionInput wrappedInput = subSection.getSubSectionInput(inputName);
			SectionInput input = designer.addSectionInput(MOCK_PREFIX + inputName, inputType.getParameterType());
			designer.link(input, wrappedInput);
		}

		@Override
		protected void loadSectionOutput(SectionOutputType outputType) throws Exception {

			// Obtain the designer
			TransformSectionDesigner designer = this.getDesginer();

			// Obtain the sub section
			SubSection subSection = designer.getSubSection(MOCK_SECTION_NAME);

			// Default map to Section Output
			String outputName = outputType.getSectionOutputName();
			SubSectionOutput wrappedOutput = subSection.getSubSectionOutput(outputName);
			SectionOutput output = designer.addSectionOutput(MOCK_PREFIX + outputName, outputType.getArgumentType(),
					outputType.isEscalationOnly());
			designer.link(wrappedOutput, output);
		}

		@Override
		protected void loadSectionObject(SectionObjectType objectType) throws Exception {

			// Obtain the designer
			TransformSectionDesigner designer = this.getDesginer();

			// Obtain the sub section
			SubSection subSection = designer.getSubSection(MOCK_SECTION_NAME);

			// Default map to Section Object
			String objectName = objectType.getSectionObjectName();
			SubSectionObject wrappedObject = subSection.getSubSectionObject(objectName);
			SectionObject object = designer.addSectionObject(MOCK_PREFIX + objectName, objectType.getObjectType());
			object.setTypeQualifier(objectType.getTypeQualifier());
			designer.link(wrappedObject, object);
		}

		@Override
		protected void loadEnhancements() throws Exception {

			// Add an enhancement by providing an additional input
			TransformSectionDesigner designer = this.getDesginer();
			SubSection subSection = designer.getSubSection(MOCK_SECTION_NAME);
			SectionInput input = designer.addSectionInput("ENHANCED_INPUT", String.class.getName());
			SubSectionInput wrappedInput = subSection.getSubSectionInput("inputTwo");
			designer.link(input, wrappedInput);

			// Must also keep inputTwo available for multiple transforms
			designer.link(designer.addSectionInput("inputTwo", String.class.getName()), wrappedInput);
		}
	}

	/**
	 * Creates the expected {@link SectionDesigner} type.
	 * 
	 * @param isTransform Indicates if wrapping.
	 * @param prefix      Prefix on the transformed {@link SectionSource}.
	 * @return Expected {@link SectionDesigner} type.
	 */
	public SectionDesigner createExpectedType(boolean isTransform, String prefix) {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil.createSectionDesigner();

		// Inputs
		type.addSectionInput(prefix + "inputOne", null);
		type.addSectionInput(prefix + "inputTwo", String.class.getName());
		if (prefix.length() > 0) {
			type.addSectionInput("inputTwo", String.class.getName());
		}

		// Outputs
		type.addSectionOutput(prefix + "one", Integer.class.getName(), false);
		type.addSectionOutput(prefix + "two", null, false);
		type.addSectionOutput(prefix + "three", char.class.getName(), false);
		type.addSectionOutput(prefix + SQLException.class.getName(), SQLException.class.getName(), true);
		type.addSectionOutput(prefix + IOException.class.getName(), IOException.class.getName(), true);
		type.addSectionOutput(prefix + NumberFormatException.class.getName(), NumberFormatException.class.getName(),
				true);

		// Objects
		SectionObject connectionSectionObject = type.addSectionObject(prefix + Connection.class.getName(),
				Connection.class.getName());

		// Type differences based on transforming
		if (isTransform) {
			// Sub section
			type.addSubSection(prefix + "TRANSFORMED", ClassSectionSource.class.getName(),
					MockSectionTypeClass.class.getName());

			// Provide the enhancements
			if (prefix.length() > 0) {
				type.addSectionInput("ENHANCED_INPUT", String.class.getName());
			}

		} else {
			// Managed Objects
			SectionManagedObjectSource objectMos = type.addSectionManagedObjectSource("OBJECT",
					ClassManagedObjectSource.class.getName());
			objectMos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					MockSectionTypeClass.class.getName());
			SectionManagedObject objectMo = objectMos.addSectionManagedObject("OBJECT", ManagedObjectScope.PROCESS);

			// Functions
			SectionFunctionNamespace namespace = type.addSectionFunctionNamespace("NAMESPACE",
					SectionClassManagedFunctionSource.class.getName());
			namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
					MockSectionTypeClass.class.getName());

			SectionFunction functionOne = namespace.addSectionFunction("inputOne", "inputOne");
			FunctionObject functionOneObject = functionOne.getFunctionObject("OBJECT");
			type.link(functionOneObject, objectMo);
			FunctionObject functionOneConnection = functionOne.getFunctionObject(Connection.class.getName());
			type.link(functionOneConnection, connectionSectionObject);

			SectionFunction functionTwo = namespace.addSectionFunction("inputTwo", "inputTwo");
			FunctionObject functionTwoObject = functionTwo.getFunctionObject("OBJECT");
			type.link(functionTwoObject, objectMo);
			functionTwo.getFunctionObject(String.class.getName()).flagAsParameter();
		}

		// Return the type
		return type;
	}

	/**
	 * Mock {@link Class} for {@link ClassSectionSource} to aid in testing
	 * {@link SectionType}.
	 */
	public static class MockSectionTypeClass {

		@FlowInterface
		public static interface Flows {

			void one(Integer parameter);

			void two();

			void three(char parameter);
		}

		public void inputOne(Connection connection, Flows flows) throws SQLException {
		}

		public Double inputTwo(@Parameter String parameter) throws IOException, NumberFormatException {
			return null;
		}
	}

	/**
	 * Mock {@link Class} for {@link ClassSectionSource} to aid in testing execution
	 * after {@link OfficeSectionTransformer}.
	 */
	public static class MockSectionRunClass {

		@FlowInterface
		public static interface Flows {
			void next(List<String> values);
		}

		public void inputTwo(@Parameter List<String> values, Flows flows) {

			// Add a value
			values.add(String.valueOf(values.size()));

			// Only stop if have all values
			if (values.size() < 2) {
				flows.next(values);
			}
		}
	}

}
