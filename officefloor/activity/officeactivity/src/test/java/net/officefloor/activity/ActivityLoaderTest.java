/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ActivityLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityLoaderTest extends OfficeFrameTestCase implements ActivityTestTrait {

	/**
	 * Mock {@link ProcedureArchitect}.
	 */
	@SuppressWarnings("unchecked")
	private final ProcedureArchitect<SubSection> procedureArchitect = this.createMock(ProcedureArchitect.class);

	/**
	 * Mock {@link ProcedureLoader}.
	 */
	private final ProcedureLoader procedureLoader = this.createMock(ProcedureLoader.class);

	/**
	 * Mock {@link SectionDesigner}.
	 */
	private final SectionDesigner sectionDesigner = this.createMock(SectionDesigner.class);

	/**
	 * Mock {@link SectionSourceContext}.
	 */
	private final SectionSourceContext sectionSourceContext = this.createMock(SectionSourceContext.class);

	/**
	 * Ensure able to load configuration.
	 */
	public void testLoadConfiguration() throws Exception {

		// Record the procedures
		SubSection procedureA = this.record_addProcedure_loadProcedureType("PROCEDURE_A",
				"net.example.ExampleProcedure", "Class", "procedure", true, (builder) -> {
					builder.addFlowType("OUTPUT_A", String.class);
					builder.addFlowType("OUTPUT_B", Long.class);
					builder.addFlowType("OUTPUT_C", null);
					builder.addFlowType("OUTPUT_D", null);
					builder.setNextArgumentType(Byte.class);
				}, "name.ONE", "value.ONE", "name.TWO", "value.TWO");
		SubSection procedureB = this.record_addProcedure_loadProcedureType("PROCEDURE_B", "net.example.KotlinProcedure",
				"Kotlin", "method", true, (builder) -> builder.setNextArgumentType(Character.class));
		SubSection procedureC = this.record_addProcedure_loadProcedureType("PROCEDURE_C", "net.example.ScalaProcedure",
				"Scala", "func", true, null);
		this.record_addProcedure_loadProcedureType("PROCEDURE_D", "net.example.JavaScriptProcedure", "JavaScript",
				"function", false, null);

		// Record the sections
		SubSection sectionA = this.record_addSection_loadSectionType("SECTION_A", "SECTION", "SECTION_LOCATION",
				(builder) -> {
					builder.addSectionInput("INPUT_A", Integer.class);
					builder.addSectionInput("INPUT_B", null);
					builder.addSectionOutput("OUTPUT_A", String.class, false);
					builder.addSectionOutput("OUTPUT_B", null, false);
					builder.addSectionOutput("OUTPUT_C", null, false);
					builder.addSectionOutput("OUTPUT_D", null, false);
				}, "name.one", "value.one", "name.two", "value.two");
		SubSection sectionB = this.record_addSection_loadSectionType("SECTION_B", "net.example.ExampleSectionSource",
				"EXAMPLE_LOCATION", (builder) -> builder.addSectionInput("INPUT_0", null));

		// Record the outputs
		SectionOutput output1 = this.record_addSectionOutput("OUTPUT_1", String.class);
		SectionOutput output2 = this.record_addSectionOutput("OUTPUT_2", null);

		// Record linking inputs
		SectionInput input1 = this.record_addSectionInput("INPUT_1", String.class);
		this.sectionDesigner.link(input1, this.input(sectionA, "INPUT_A"));
		SectionInput input2 = this.record_addSectionInput("INPUT_2", Integer.class);
		this.sectionDesigner.link(input2, this.procedure(procedureA));
		SectionInput input3 = this.record_addSectionInput("INPUT_3", null);
		this.sectionDesigner.link(input3, output2);
		this.record_addSectionInput("INPUT_4", null);

		// Record link procedures
		this.sectionDesigner.link(this.procedureNext(procedureA), this.input(sectionA, "INPUT_A"));
		this.sectionDesigner.link(this.output(procedureA, "OUTPUT_A"), this.input(sectionB, "INPUT_0"));
		this.sectionDesigner.link(this.output(procedureA, "OUTPUT_B"), this.procedure(procedureB));
		this.sectionDesigner.link(this.output(procedureA, "OUTPUT_C"), output1);
		this.sectionDesigner.link(this.procedureNext(procedureB), this.procedure(procedureB));
		this.sectionDesigner.link(this.procedureNext(procedureC), output2);

		// Record link sections
		this.sectionDesigner.link(this.output(sectionA, "OUTPUT_A"), this.input(sectionB, "INPUT_0"));
		this.sectionDesigner.link(this.output(sectionA, "OUTPUT_B"), this.procedure(procedureB));
		this.sectionDesigner.link(this.output(sectionA, "OUTPUT_C"), output2);

		// Load the activity
		ConfigurationItem item = this.createConfigurationItem("LoadConfiguration.activity.xml");
		this.replayMockObjects();
		new ActivityLoaderImpl().loadActivityConfiguration(new MockActivityContext(item));
		this.verifyMockObjects();
	}

	/**
	 * Records adding a {@link SectionInput}.
	 * 
	 * @param inputName     Name of {@link SectionInput}.
	 * @param parameterType Parameter type.
	 * @return {@link SectionInput}.
	 */
	private SectionInput record_addSectionInput(String inputName, Class<?> parameterType) {
		SectionInput input = this.createMock(SectionInput.class);
		String parameterTypeName = parameterType == null ? null : parameterType.getName();
		this.recordReturn(this.sectionDesigner, this.sectionDesigner.addSectionInput(inputName, parameterTypeName),
				input);
		return input;
	}

	/**
	 * Records adding a {@link Procedure}.
	 * 
	 * @param sectionName        Name of {@link Procedure}.
	 * @param resource           Resource.
	 * @param sourceName         Source name.
	 * @param procedureName      {@link Procedure} name.
	 * @param isNext             If is next connected.
	 * @param constructor        {@link ProcedureTypeConstructor}.
	 * @param propertyNameValues {@link PropertyList} name/value pairs.
	 * @return {@link SubSection} for the {@link Procedure}.
	 */
	private SubSection record_addProcedure_loadProcedureType(String sectionName, String resource, String sourceName,
			String procedureName, boolean isNext, ProcedureTypeConstructor constructor, String... propertyNameValues) {

		// Record the properties
		PropertyList properties = this.record_createPropertyList(propertyNameValues);

		// Record adding the procedure
		SubSection section = this.createMock(SubSection.class);
		this.recordReturn(this.procedureArchitect, this.procedureArchitect.addProcedure(sectionName, resource,
				sourceName, procedureName, isNext, properties), section);

		// Record loading the procedure type
		ProcedureType type = this.constructProcedureType(procedureName, null, constructor);
		this.recordReturn(this.procedureLoader,
				this.procedureLoader.loadProcedureType(resource, sourceName, procedureName, properties), type);

		// Return the section for the procedure
		return section;
	}

	/**
	 * Records getting {@link SubSectionInput}.
	 * 
	 * @param section   {@link SubSection}.
	 * @param inputName Name of {@link SubSectionInput}.
	 * @return {@link SubSectionInput}.
	 */
	private SubSectionInput input(SubSection section, String inputName) {
		SubSectionInput input = this.createMock(SubSectionInput.class);
		this.recordReturn(section, section.getSubSectionInput(inputName), input);
		return input;
	}

	/**
	 * Records getting {@link Procedure} {@link SubSectionInput}.
	 * 
	 * @param procedure {@link SubSection} for {@link Procedure}.
	 * @return {@link SubSectionInput} to {@link Procedure}.
	 */
	private SubSectionInput procedure(SubSection procedure) {
		return this.input(procedure, ProcedureArchitect.INPUT_NAME);
	}

	/**
	 * Records getting {@link SubSectionOutput}.
	 * 
	 * @param section    {@link SubSection}.
	 * @param outputName Name of {@link SubSectionOutput}.
	 * @return {@link SubSectionOutput}.
	 */
	private SubSectionOutput output(SubSection section, String outputName) {
		SubSectionOutput output = this.createMock(SubSectionOutput.class);
		this.recordReturn(section, section.getSubSectionOutput(outputName), output);
		return output;
	}

	/**
	 * Records getting {@link Procedure} next.
	 * 
	 * @param procedure {@link SubSection} for {@link Procedure}.
	 * @return {@link SubSectionOutput} for {@link Procedure} next.
	 */
	private SubSectionOutput procedureNext(SubSection procedure) {
		return this.output(procedure, ProcedureArchitect.NEXT_OUTPUT_NAME);
	}

	/**
	 * Records add a {@link SubSection}.
	 * 
	 * @param sectionName            Name of {@link SubSection}.
	 * @param sectionSourceClassName {@link SectionSource} {@link Class} name.
	 * @param location               Location.
	 * @param constructor            {@link SectionTypeConstructor}.
	 * @param propertyNameValues     {@link PropertyList} name/value pairs.
	 * @return {@link SubSection}.
	 */
	private SubSection record_addSection_loadSectionType(String sectionName, String sectionSourceClassName,
			String location, SectionTypeConstructor constructor, String... propertyNameValues) {

		// Record the properties
		PropertyList properties = this.record_createPropertyList(propertyNameValues);

		// Record adding the section
		SubSection section = this.createMock(SubSection.class);
		this.recordReturn(this.sectionDesigner,
				this.sectionDesigner.addSubSection(sectionName, sectionSourceClassName, location), section);
		properties.configureProperties(section);

		// Record loading the section type
		SectionType type = this.constructSectionType(constructor);
		this.recordReturn(this.sectionSourceContext,
				this.sectionSourceContext.loadSectionType(sectionName, sectionSourceClassName, location, properties),
				type);

		// Return the section
		return section;
	}

	/**
	 * Records creating a {@link PropertyList}.
	 * 
	 * @param propertyNameValues {@link PropertyList} name/value pairs.
	 * @return {@link PropertyList}.
	 */
	private PropertyList record_createPropertyList(String... propertyNameValues) {
		PropertyList properties = this.createMock(PropertyList.class);
		this.recordReturn(this.sectionSourceContext, this.sectionSourceContext.createPropertyList(), properties);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			Property property = this.createMock(Property.class);
			this.recordReturn(properties, properties.addProperty(name), property);
			property.setValue(value);
		}
		return properties;
	}

	/**
	 * Records adding a {@link SectionOutput}.
	 * 
	 * @param outputName   Name of {@link SectionOutput}.
	 * @param argumentType Argument type.
	 * @return {@link SectionOutput}.
	 */
	private SectionOutput record_addSectionOutput(String outputName, Class<?> argumentType) {
		SectionOutput output = this.createMock(SectionOutput.class);
		String argumentTypeName = argumentType == null ? null : argumentType.getName();
		this.recordReturn(this.sectionDesigner,
				this.sectionDesigner.addSectionOutput(outputName, argumentTypeName, false), output);
		return output;
	}

	/**
	 * Creates the {@link ConfigurationItem}.
	 * 
	 * @param fileName Name of {@link ConfigurationItem} file.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem createConfigurationItem(String fileName) {
		String filePath = this.getClass().getPackage().getName().replace('.', '/') + "/" + fileName;
		ConfigurationItem item = new ClassLoaderConfigurationContext(this.getClass().getClassLoader(), null)
				.getConfigurationItem(filePath, null);
		assertNotNull("Can not find configuration " + fileName, item);
		return item;
	}

	/**
	 * Mock {@link ActivityContext}.
	 */
	private class MockActivityContext implements ActivityContext {

		/**
		 * {@link ConfigurationItem}.
		 */
		private ConfigurationItem configurationItem;

		/**
		 * Instantiate.
		 * 
		 * @param configurationItem {@link ConfigurationItem}.
		 */
		private MockActivityContext(ConfigurationItem configurationItem) {
			this.configurationItem = configurationItem;
		}

		/*
		 * ================== ActivityContext ======================
		 */

		@Override
		public ConfigurationItem getConfiguration() {
			return this.configurationItem;
		}

		@Override
		public ProcedureArchitect<SubSection> getProcedureArchitect() {
			return ActivityLoaderTest.this.procedureArchitect;
		}

		@Override
		public ProcedureLoader getProcedureLoader() {
			return ActivityLoaderTest.this.procedureLoader;
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return ActivityLoaderTest.this.sectionDesigner;
		}

		@Override
		public SectionSourceContext getSectionSourceContext() {
			return ActivityLoaderTest.this.sectionSourceContext;
		}
	}

}