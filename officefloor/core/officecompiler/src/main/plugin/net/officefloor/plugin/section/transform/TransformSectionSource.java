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

package net.officefloor.plugin.section.transform;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSectionTransformer;
import net.officefloor.compile.spi.office.OfficeSectionTransformerContext;
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
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.source.PrivateSource;

/**
 * Enables transforming a {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class TransformSectionSource extends AbstractSectionSource implements OfficeSectionTransformer {

	/**
	 * Name of the {@link Property} for the {@link Class} name of the
	 * {@link SectionSource} to transform.
	 */
	public static final String PROPERTY_SECTION_SOURCE_CLASS_NAME = "transform.section.source";

	/**
	 * Name of the {@link Property} for the location of the {@link SectionSource} to
	 * transform.
	 */
	public static final String PROPERTY_SECTION_LOCATION = "transform.section.location";

	/**
	 * Prefix on the {@link Property} name for the {@link Property} instances of the
	 * {@link SectionSource} to transform.
	 */
	public static final String PROPERTY_SECTION_PROPERTY_PREFIX = "transform.section.property.prefix.";

	/**
	 * Name of the {@link SubSection} being transformed.
	 */
	public static final String SUB_SECTION_NAME = "TRANSFORMED";

	/**
	 * {@link TransformSectionDesigner}.
	 */
	private TransformSectionDesigner transformDesigner;

	/**
	 * {@link SectionSourceContext}.
	 */
	private SectionSourceContext context;

	/**
	 * Obtains the {@link TransformSectionDesigner} to configure the transformed
	 * {@link SectionSource}.
	 * 
	 * @return {@link TransformSectionDesigner}.
	 */
	protected final TransformSectionDesigner getDesginer() {
		return this.transformDesigner;
	}

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	protected final SectionSourceContext getContext() {
		return this.context;
	}

	/**
	 * <p>
	 * Loads the {@link SectionSource}.
	 * <p>
	 * Override to alter the {@link SectionSource}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @param properties
	 *            {@link PropertyList} for the {@link SectionSource}.
	 * @throws Exception
	 *             If fails to load the wrapped {@link SubSection}.
	 */
	protected void loadSubSection(String sectionSourceClassName, String sectionLocation, PropertyList properties)
			throws Exception {
		SubSection subSection = this.getDesginer().addSubSection(SUB_SECTION_NAME, sectionSourceClassName,
				sectionLocation);
		for (Property property : properties) {
			subSection.addProperty(property.getName(), property.getValue());
		}
	}

	/**
	 * <p>
	 * Loads the {@link SectionInput}.
	 * <p>
	 * Override to alter {@link SectionInput} instances.
	 * 
	 * @param inputType
	 *            {@link SectionInputType}.
	 * @throws Exception
	 *             If fails to load the {@link SectionInput}.
	 */
	protected void loadSectionInput(SectionInputType inputType) throws Exception {

		// Obtain the designer
		TransformSectionDesigner designer = this.getDesginer();

		// Obtain the sub section
		SubSection subSection = designer.getSubSection(SUB_SECTION_NAME);

		// Default map to Section Input
		String inputName = inputType.getSectionInputName();
		SubSectionInput wrappedInput = subSection.getSubSectionInput(inputName);
		SectionInput input = designer.addSectionInput(inputName, inputType.getParameterType());
		designer.link(input, wrappedInput);
	}

	/**
	 * <p>
	 * Loads the {@link SectionOutput}.
	 * <p>
	 * Override to alter {@link SectionOutput} instances.
	 * 
	 * @param outputType
	 *            {@link SectionOutputType}.
	 * @throws Exception
	 *             If fails to load the {@link SectionOutput}.
	 */
	protected void loadSectionOutput(SectionOutputType outputType) throws Exception {

		// Obtain the designer
		TransformSectionDesigner designer = this.getDesginer();

		// Obtain the sub section
		SubSection subSection = designer.getSubSection(SUB_SECTION_NAME);

		// Default map to Section Output
		String outputName = outputType.getSectionOutputName();
		SubSectionOutput wrappedOutput = subSection.getSubSectionOutput(outputName);
		SectionOutput output = designer.addSectionOutput(outputName, outputType.getArgumentType(),
				outputType.isEscalationOnly());
		designer.link(wrappedOutput, output);
	}

	/**
	 * <p>
	 * Loads the {@link SectionObject}.
	 * <p>
	 * Override to alter {@link SectionObject} instances.
	 * 
	 * @param objectType
	 *            {@link SectionObjectType}.
	 * @throws Exception
	 *             If fails to laod the {@link SectionObject}.
	 */
	protected void loadSectionObject(SectionObjectType objectType) throws Exception {

		// Obtain the designer
		TransformSectionDesigner designer = this.getDesginer();

		// Obtain the sub section
		SubSection subSection = designer.getSubSection(SUB_SECTION_NAME);

		// Default map to Section Object
		String objectName = objectType.getSectionObjectName();
		SubSectionObject wrappedObject = subSection.getSubSectionObject(objectName);
		SectionObject object = designer.addSectionObject(objectName, objectType.getObjectType());
		object.setTypeQualifier(objectType.getTypeQualifier());
		designer.link(wrappedObject, object);
	}

	/**
	 * <p>
	 * Loads further enhancements.
	 * <p>
	 * Override to load further enhancements.
	 * 
	 * @throws Exception
	 *             If fails to load enhancements.
	 */
	protected void loadEnhancements() throws Exception {
		// By default, no enhancements
	}

	/*
	 * ====================== OfficeSectionTransformer =====================
	 */

	@Override
	public void transformOfficeSection(OfficeSectionTransformerContext context) {

		// Create the properties
		PropertyList properties = context.createPropertyList();
		properties.addProperty(PROPERTY_SECTION_SOURCE_CLASS_NAME).setValue(context.getSectionSourceClassName());
		String location = context.getSectionLocation();
		if (location != null) {
			properties.addProperty(PROPERTY_SECTION_LOCATION).setValue(location);
		}
		for (Property property : context.getSectionProperties()) {
			properties.addProperty(PROPERTY_SECTION_PROPERTY_PREFIX + property.getName()).setValue(property.getValue());
		}

		// Load additional properties
		this.configureProperties(context, properties);

		// Transform the section
		context.setTransformedOfficeSection(this.getClass().getName(), null, properties);
	}

	/**
	 * Enables overriding to configure additional {@link Property} instances.
	 * 
	 * @param context
	 *            {@link OfficeSectionTransformerContext}.
	 * @param properties
	 *            {@link PropertyList} to load additional {@link Property}
	 *            instances.
	 */
	protected void configureProperties(OfficeSectionTransformerContext context, PropertyList properties) {
		// No additional properties
	}

	/*
	 * =================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SECTION_SOURCE_CLASS_NAME, "Section Source");
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Load state for override methods
		this.transformDesigner = new TransformSectionDesigner(designer);
		this.context = context;

		// Obtain the section details to transform
		String sectionSourceClassName = context.getProperty(PROPERTY_SECTION_SOURCE_CLASS_NAME);
		String sectionLocation = context.getProperty(PROPERTY_SECTION_LOCATION, null);

		// Obtain the properties for the section
		PropertyList sectionProperties = context.createPropertyList();
		for (String propertyName : context.getPropertyNames()) {
			if (propertyName.startsWith(PROPERTY_SECTION_PROPERTY_PREFIX)) {

				// Section property, so obtain the property details
				String propertyValue = context.getProperty(propertyName);
				String sectionPropertyName = propertyName.substring(PROPERTY_SECTION_PROPERTY_PREFIX.length());

				// Add the section property
				sectionProperties.addProperty(sectionPropertyName).setValue(propertyValue);
			}
		}

		// Obtain the section type
		SectionType type = context.loadSectionType("TRANSFORM", sectionSourceClassName, sectionLocation,
				sectionProperties);

		// Add the section to transform
		this.loadSubSection(sectionSourceClassName, sectionLocation, sectionProperties);

		// Map the inputs
		for (SectionInputType inputType : type.getSectionInputTypes()) {
			this.loadSectionInput(inputType);
		}

		// Map the outputs
		for (SectionOutputType outputType : type.getSectionOutputTypes()) {
			this.loadSectionOutput(outputType);
		}

		// Map the objects
		for (SectionObjectType objectType : type.getSectionObjectTypes()) {
			this.loadSectionObject(objectType);
		}

		// Load the enhancements
		this.loadEnhancements();
	}

}
