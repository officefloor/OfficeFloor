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

package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.PropertyModel;

/**
 * Abstract functionality for refactoring {@link OfficeSectionModel} to an
 * {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRefactorOfficeSectionTest extends
		AbstractOfficeChangesTestCase {

	/**
	 * {@link OfficeSectionModel} to refactor.
	 */
	private OfficeSectionModel sectionModel;

	/**
	 * Name to refactor the {@link OfficeSectionModel} to have.
	 */
	private String sectionName;

	/**
	 * {@link SectionSource} class name to refactor the
	 * {@link OfficeSectionModel} to have.
	 */
	private String sectionSourceClassName;

	/**
	 * Location to refactor the {@link OfficeSectionModel} to have.
	 */
	private String sectionLocation;

	/**
	 * {@link PropertyList} to refactor the {@link OfficeSectionModel} to have.
	 */
	private PropertyList properties = null;

	/**
	 * Mapping of {@link OfficeSectionInput} name to
	 * {@link OfficeSectionInputModel} name.
	 */
	private final Map<String, String> inputNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeSectionOutput} name to
	 * {@link OfficeSectionOutputModel} name.
	 */
	private final Map<String, String> outputNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeSectionObject} name to
	 * {@link OfficeSectionObjectModel} name.
	 */
	private final Map<String, String> objectNameMapping = new HashMap<String, String>();

	/**
	 * Initiate for specific setup per test.
	 */
	public AbstractRefactorOfficeSectionTest() {
		super(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.impl.AbstractChangesTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Setup the model
		super.setUp();

		// Obtain office section model and specify details from it
		this.sectionModel = this.model.getOfficeSections().get(0);
		this.sectionName = this.sectionModel.getOfficeSectionName();
		this.sectionSourceClassName = this.sectionModel
				.getSectionSourceClassName();
		this.sectionLocation = this.sectionModel.getSectionLocation();
	}

	/**
	 * Refactors the {@link OfficeSectionModel} name.
	 *
	 * @param newName
	 *            New name for the {@link OfficeSectionModel}.
	 */
	protected void refactor_officeSectionName(String newName) {
		this.sectionName = newName;
	}

	/**
	 * Refactors the {@link SectionSource} class name.
	 *
	 * @param sectionSourceClassName
	 *            New {@link SectionSource} class name.
	 */
	protected void refactor_sectionSourceClassName(String sectionSourceClassName) {
		this.sectionSourceClassName = sectionSourceClassName;
	}

	/**
	 * Refactors the location of the {@link OfficeSectionModel}.
	 *
	 * @param sectionLocation
	 *            Location of the {@link OfficeSectionModel}.
	 */
	protected void refactor_sectionLocation(String sectionLocation) {
		this.sectionLocation = sectionLocation;
	}

	/**
	 * Refactors by adding a {@link Property}.
	 *
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	protected void refactor_addProperty(String name, String value) {

		// Obtain the properties
		if (this.properties == null) {
			this.properties = new PropertyListImpl();
		}

		// Add the property
		this.properties.addProperty(name).setValue(value);
	}

	/**
	 * Maps {@link OfficeSectionInput} name to {@link OfficeSectionObjectModel}
	 * name.
	 *
	 * @param inputTypeName
	 *            {@link OfficeSectionInput} name.
	 * @param inputModelName
	 *            {@link OfficeSectionObjectModel} name.
	 */
	protected void refactor_mapInput(String inputTypeName, String inputModelName) {
		this.inputNameMapping.put(inputModelName, inputModelName);
	}

	/**
	 * Maps {@link OfficeSectionOutput} name to {@link OfficeSectionOutputModel}
	 * name.
	 *
	 * @param outputTypeName
	 *            {@link OfficeSectionOutput} name.
	 * @param outputModelName
	 *            {@link OfficeSectionOutputModel} name.
	 */
	protected void refactor_mapOutput(String outputTypeName,
			String outputModelName) {
		this.outputNameMapping.put(outputTypeName, outputModelName);
	}

	/**
	 * Maps {@link OfficeSectionObject} name to {@link OfficeSectionObjectModel}
	 * name.
	 *
	 * @param objectTypeName
	 *            {@link OfficeSectionObject} name.
	 * @param objectModelName
	 *            {@link OfficeSectionObjectModel} name.
	 */
	protected void refactor_mapObject(String objectTypeName,
			String objectModelName) {
		this.objectNameMapping.put(objectTypeName, objectModelName);
	}

	/**
	 * Convenience method to do refactoring with a simple
	 * {@link OfficeSectionType}.
	 */
	protected void doRefactor() {
		this.doRefactor((OfficeSectionConstructor) null);
	}

	/**
	 * Convenience method to do refactoring from {@link OfficeSection} from the
	 * {@link OfficeSectionConstructor}.
	 *
	 * @param constructor
	 *            {@link OfficeSectionConstructor}.
	 */
	protected void doRefactor(OfficeSectionConstructor constructor) {
		OfficeSectionType sectionType = this
				.constructOfficeSectionType(constructor);
		this.doRefactor(sectionType);
	}

	/**
	 * Does the refactoring and validates applying and reverting.
	 *
	 * @param officeSectionType
	 *            {@link OfficeSectionType}.
	 */
	protected void doRefactor(OfficeSectionType officeSectionType) {

		// Create the property list
		PropertyList propertyList = this.properties;
		if (propertyList == null) {
			// Not refactoring properties, so take from work model
			propertyList = new PropertyListImpl();
			for (PropertyModel property : this.sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}
		}

		// Create the change to refactor the office section
		Change<OfficeSectionModel> change = this.operations
				.refactorOfficeSection(this.sectionModel, this.sectionName,
						this.sectionSourceClassName, this.sectionLocation,
						propertyList, officeSectionType, this.inputNameMapping,
						this.outputNameMapping, this.objectNameMapping);

		// Assert the refactoring changes
		this.assertChange(change, this.sectionModel, "Refactor office section",
				true);
	}

}
