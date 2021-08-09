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

package net.officefloor.model.impl.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

/**
 * Abstract functionality for refactoring the {@link DeployedOfficeModel}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRefactorDeployedOfficeTest extends
		AbstractOfficeFloorChangesTestCase {

	/**
	 * {@link DeployedOfficeModel} to refactor.
	 */
	private DeployedOfficeModel officeModel;

	/**
	 * Name to refactor the {@link DeployedOfficeModel} to have.
	 */
	private String officeName;

	/**
	 * {@link OfficeSource} class name to refactor the
	 * {@link DeployedOfficeModel} to have.
	 */
	private String officeSourceClassName;

	/**
	 * Location to refactor the {@link DeployedOfficeModel} to have.
	 */
	private String officeLocation;

	/**
	 * {@link PropertyList} to refactor the {@link OfficeSectionModel} to have.
	 */
	private PropertyList properties = null;

	/**
	 * Mapping of {@link OfficeManagedObjectType} name to
	 * {@link DeployedOfficeObjectModel} name.
	 */
	private final Map<String, String> objectNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeAvailableSectionInputType} name to
	 * {@link DeployedOfficeInputModel} name.
	 */
	private final Map<String, String> inputNameMapping = new HashMap<String, String>();

	/**
	 * Mapping of {@link OfficeTeamType} name to {@link DeployedOfficeTeamModel}
	 * name.
	 */
	private final Map<String, String> teamNameMapping = new HashMap<String, String>();

	/**
	 * Initiate for specific setup per test.
	 */
	public AbstractRefactorDeployedOfficeTest() {
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

		// Obtain deployed office model and specify details from it
		this.officeModel = this.model.getDeployedOffices().get(0);
		this.officeName = this.officeModel.getDeployedOfficeName();
		this.officeSourceClassName = this.officeModel
				.getOfficeSourceClassName();
		this.officeLocation = this.officeModel.getOfficeLocation();
	}

	/**
	 * Sets up refactoring for the {@link DeployedOfficeModel} to have the new
	 * name.
	 *
	 * @param newOfficeName
	 *            New {@link DeployedOfficeModel} name.
	 */
	protected void refactor_deployedOfficeName(String newOfficeName) {
		this.officeName = newOfficeName;
	}

	/**
	 * Sets up refactoring of the {@link OfficeSource}.
	 *
	 * @param officeSourceClassName
	 *            New {@link OfficeSource} class name.
	 */
	protected void refactor_officeSourceClassName(String officeSourceClassName) {
		this.officeSourceClassName = officeSourceClassName;
	}

	/**
	 * Sets up refactoring of the {@link DeployedOfficeModel} location.
	 *
	 * @param officeLocation
	 *            {@link DeployedOfficeModel} location.
	 */
	protected void refactor_officeLocation(String officeLocation) {
		this.officeLocation = officeLocation;
	}

	/**
	 * Sets up refactoring a change in {@link PropertyList}.
	 *
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	protected void refactor_addProperty(String name, String value) {

		// Obtain the property list
		if (this.properties == null) {
			this.properties = new PropertyListImpl();
		}

		// Add the property
		this.properties.addProperty(name).setValue(value);
	}

	/**
	 * Maps {@link OfficeManagedObjectType} name to
	 * {@link DeployedOfficeObjectModel} name.
	 *
	 * @param objectTypeName
	 *            {@link OfficeManagedObjectType} name.
	 * @param objectModelName
	 *            {@link DeployedOfficeObjectModel} name.
	 */
	protected void refactor_mapObject(String objectTypeName,
			String objectModelName) {
		this.objectNameMapping.put(objectTypeName, objectModelName);
	}

	/**
	 * Maps {@link OfficeAvailableSectionInputType} name to {@link DeployedOfficeInputModel}
	 * name.
	 *
	 * @param inputTypeName
	 *            {@link OfficeAvailableSectionInputType} name.
	 * @param inputModelName
	 *            {@link DeployedOfficeInputModel} name.
	 */
	protected void refactor_mapInput(String inputTypeName, String inputModelName) {
		this.inputNameMapping.put(inputTypeName, inputModelName);
	}

	/**
	 * Maps {@link OfficeTeamType} name to {@link DeployedOfficeTeamModel} name.
	 *
	 * @param teamTypeName
	 *            {@link OfficeTeamType} name.
	 * @param teamModelName
	 *            {@link DeployedOfficeTeamModel} name.
	 */
	protected void refactor_mapTeam(String teamTypeName, String teamModelName) {
		this.teamNameMapping.put(teamTypeName, teamModelName);
	}

	/**
	 * Does refactoring test with a simple {@link OfficeType}.
	 */
	protected void doRefactor() {
		this.doRefactor((OfficeType) null);
	}

	/**
	 * Convenience method to do refactoring.
	 *
	 * @param constructor
	 *            {@link OfficeTypeConstructor}.
	 */
	protected void doRefactor(OfficeTypeConstructor constructor) {
		OfficeType officeType = this.constructOfficeType(constructor);
		this.doRefactor(officeType);
	}

	/**
	 * Does the refactoring testing.
	 *
	 * @param officeType
	 *            {@link OfficeType} to refactor the {@link DeployedOfficeModel}
	 *            to.
	 */
	protected void doRefactor(OfficeType officeType) {

		// Ensure have office type
		if (officeType == null) {
			// Provide simple office type
			officeType = this.constructOfficeType(new OfficeTypeConstructor() {
				@Override
				public void construct(OfficeTypeContext context) {
					// Keep simple
				}
			});
		}

		// Create the property list
		PropertyList propertyList = this.properties;
		if (propertyList == null) {
			// Not refactoring properties, so take from office model
			propertyList = new PropertyListImpl();
			for (PropertyModel property : this.officeModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}
		}

		// Create the change to refactor the deployed office
		Change<DeployedOfficeModel> change = this.operations
				.refactorDeployedOffice(this.officeModel, this.officeName,
						this.officeSourceClassName, this.officeLocation,
						propertyList, officeType, this.objectNameMapping,
						this.inputNameMapping, this.teamNameMapping);

		// Assert the refactoring changes
		this.assertChange(change, this.officeModel, "Refactor deployed office",
				true);
	}
}
