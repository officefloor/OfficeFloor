/*-
 * #%L
 * Activity
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

package net.officefloor.activity;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.escalate.Escalation;
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
					builder.addSectionOutput("OUTPUT_A", String.class);
					builder.addSectionOutput("OUTPUT_B", null);
					builder.addSectionOutput("OUTPUT_C", null);
					builder.addSectionOutput("OUTPUT_D", null);
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
		this.loadActivity("Configuration.activity.xml");
	}

	/**
	 * Ensure all objects are made available for linking.
	 */
	public void testObjects() throws Exception {

		// Record procedures
		SubSection procedure = this.record_addProcedure_loadProcedureType("PROCEDURE", "net.example.Example", "Class",
				"function", false, (builder) -> {
					builder.addObjectType("OBJECT_A", Object.class, null);
					builder.addObjectType("OBJECT_B", Object.class, "qualified");
					builder.addObjectType("OBJECT_C", Object.class, "another");
					builder.addObjectType("OBJECT_D", String.class, null);
				});
		SectionObject object = this.record_addSectionObject(Object.class, null);
		this.sectionDesigner.link(this.object(procedure, "OBJECT_A"), object);
		SectionObject qualifiedObject = this.record_addSectionObject(Object.class, "qualified");
		this.sectionDesigner.link(this.object(procedure, "OBJECT_B"), qualifiedObject);
		this.sectionDesigner.link(this.object(procedure, "OBJECT_C"),
				this.record_addSectionObject(Object.class, "another"));
		this.sectionDesigner.link(this.object(procedure, "OBJECT_D"), this.record_addSectionObject(String.class, null));

		// Record sections
		SubSection section = this.record_addSection_loadSectionType("SECTION", "net.example.Example",
				"EXAMPLE_LOCATION", (builder) -> {
					builder.addSectionObject("OBJECT_1", Object.class, null);
					builder.addSectionObject("OBJECT_2", Object.class, "qualified");
					builder.addSectionObject("OBJECT_3", Object.class, "different");
					builder.addSectionObject("OBJECT_4", Integer.class, null);
				});
		this.sectionDesigner.link(this.object(section, "OBJECT_1"), object);
		this.sectionDesigner.link(this.object(section, "OBJECT_2"), qualifiedObject);
		this.sectionDesigner.link(this.object(section, "OBJECT_3"),
				this.record_addSectionObject(Object.class, "different"));
		this.sectionDesigner.link(this.object(section, "OBJECT_4"), this.record_addSectionObject(Integer.class, null));

		// Load the activity
		this.loadActivity("Objects.activity.xml");
	}

	/**
	 * Ensure handle {@link Exception}.
	 */
	public void testExceptions() throws Exception {

		// Record procedures
		SubSection procedure = this.record_addProcedure_loadProcedureType("PROCEDURE", "net.example.Example", "Class",
				"function", false, (builder) -> {
					builder.addEscalationType(FileNotFoundException.class);
					builder.addEscalationType(EOFException.class);
					builder.addEscalationType(IOException.class);
					builder.addEscalationType(SQLException.class);
					builder.addEscalationType(NullPointerException.class);
					builder.addEscalationType(RuntimeException.class);
				});

		// Record sections
		SubSection section = this.record_addSection_loadSectionType("SECTION", "net.example.Example",
				"EXAMPLE_LOCATION", (builder) -> {
					builder.addSectionInput("INPUT", IOException.class);
					builder.addSectionEscalation(FileNotFoundException.class);
					builder.addSectionEscalation(EOFException.class);
					builder.addSectionEscalation(SQLException.class);
					builder.addSectionEscalation(IOException.class);
					builder.addSectionEscalation(NoSuchElementException.class);
					builder.addSectionEscalation(RuntimeException.class);
				});

		// Record outputs
		SectionOutput output = this.record_addSectionOutput("OUTPUT", null);

		// Record loading escalations
		this.record_loadClass(IOException.class);
		this.record_loadClass(FileNotFoundException.class);
		this.record_loadClass(SQLException.class);

		// Record linking procedure escalations
		this.sectionDesigner.link(this.escalation(procedure, FileNotFoundException.class), this.procedure(procedure));
		this.sectionDesigner.link(this.escalation(procedure, EOFException.class), this.input(section, "INPUT"));
		this.sectionDesigner.link(this.escalation(procedure, IOException.class), this.input(section, "INPUT"));
		this.sectionDesigner.link(this.escalation(procedure, SQLException.class), output);
		this.sectionDesigner.link(this.escalation(procedure, NullPointerException.class),
				this.record_addSectionEscalation("PROCEDURE-" + NullPointerException.class.getName(),
						NullPointerException.class));
		this.sectionDesigner.link(this.escalation(procedure, RuntimeException.class), this
				.record_addSectionEscalation("PROCEDURE-" + RuntimeException.class.getName(), RuntimeException.class));

		// Record linking section escalations
		this.record_loadClass(EOFException.class);
		this.sectionDesigner.link(this.escalation(section, EOFException.class), this.input(section, "INPUT"));
		this.record_loadClass(FileNotFoundException.class);
		this.sectionDesigner.link(this.escalation(section, FileNotFoundException.class), this.procedure(procedure));
		this.record_loadClass(IOException.class);
		this.sectionDesigner.link(this.escalation(section, IOException.class), this.input(section, "INPUT"));
		this.record_loadClass(RuntimeException.class);
		this.sectionDesigner.link(this.escalation(section, RuntimeException.class), this
				.record_addSectionEscalation("SECTION-" + RuntimeException.class.getName(), RuntimeException.class));
		this.record_loadClass(SQLException.class);
		this.sectionDesigner.link(this.escalation(section, SQLException.class), output);
		this.record_loadClass(NoSuchElementException.class);
		this.sectionDesigner.link(this.escalation(section, NoSuchElementException.class),
				this.record_addSectionEscalation("SECTION-" + NoSuchElementException.class.getName(),
						NoSuchElementException.class));

		// Load the activity
		this.loadActivity("Exceptions.activity.xml");
	}

	/**
	 * Loads the {@link ActivityModel} from the file via the {@link ActivityLoader}.
	 * 
	 * @param fileName Name of file.
	 * @throws Exception If fails to load {@link ActivityModel}.
	 */
	private void loadActivity(String fileName) throws Exception {

		// Obtain the configuration item
		String filePath = this.getClass().getPackage().getName().replace('.', '/') + "/" + fileName;
		ConfigurationItem item = new ClassLoaderConfigurationContext(this.getClass().getClassLoader(), null)
				.getConfigurationItem(filePath, null);
		assertNotNull("Can not find configuration " + fileName, item);

		// Load the activity
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
	 * Records adding a {@link SectionObject}.
	 * 
	 * @param objectType    Object type.
	 * @param typeQualifier Type qualifier.
	 * @return {@link SectionObject}.
	 */
	private SectionObject record_addSectionObject(Class<?> objectType, String typeQualifier) {

		// Derive the object name
		String objectName = (typeQualifier == null ? "" : typeQualifier + "-") + objectType.getName();

		// Record adding the section object
		SectionObject object = this.createMock(SectionObject.class);
		String objectTypeName = (objectType == null) ? null : objectType.getName();
		this.recordReturn(this.sectionDesigner, this.sectionDesigner.addSectionObject(objectName, objectTypeName),
				object);
		if (typeQualifier != null) {
			object.setTypeQualifier(typeQualifier);
		}
		return object;
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
	 * Records getting {@link SubSectionObject}.
	 * 
	 * @param section    {@link SubSection}.
	 * @param objectName Name of {@link SubSectionObject}.
	 * @return {@link SubSectionObject}.
	 */
	private SubSectionObject object(SubSection section, String objectName) {
		SubSectionObject object = this.createMock(SubSectionObject.class);
		this.recordReturn(section, section.getSubSectionObject(objectName), object);
		return object;
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
	 * Records getting {@link SubSectionOutput} for {@link Escalation}
	 * 
	 * @param section        {@link SubSection}.
	 * @param escalationType Type of {@link Escalation}.
	 * @return {@link SubSectionOutput}.
	 */
	private SubSectionOutput escalation(SubSection section, Class<? extends Throwable> escalationType) {
		return this.output(section, escalationType.getName());
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
	 * Records adding a {@link SectionOutput} for {@link Escalation}.
	 * 
	 * @param outputName   Name of {@link SectionOutput}.
	 * @param argumentType Argument type.
	 * @return {@link SectionOutput}.
	 */
	private SectionOutput record_addSectionEscalation(String outputName, Class<? extends Throwable> argumentType) {
		SectionOutput output = this.createMock(SectionOutput.class);
		String argumentTypeName = argumentType == null ? null : argumentType.getName();
		this.recordReturn(this.sectionDesigner,
				this.sectionDesigner.addSectionOutput(outputName, argumentTypeName, true), output);
		return output;
	}

	/**
	 * Records loading {@link Escalation} {@link Class}.
	 * 
	 * @param <E>             {@link Escalation} type.
	 * @param escalationClass {@link Escalation} {@link Class}.
	 */
	private <E extends Throwable> void record_loadClass(Class<E> escalationClass) {
		this.recordReturn(this.sectionSourceContext, this.sectionSourceContext.loadClass(escalationClass.getName()),
				escalationClass);
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
