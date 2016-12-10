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
package net.officefloor.eclipse.wizard.officesource;

import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

/**
 * Instance of a {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInstance {

	/**
	 * Name of this {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link OfficeSource} class name.
	 */
	private final String officeSourceClassName;

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link DeployedOfficeModel}.
	 */
	private final DeployedOfficeModel deployedOfficeModel;

	/**
	 * {@link OfficeType}.
	 */
	private final OfficeType officeType;

	/**
	 * Mapping of {@link OfficeManagedObjectType} name to
	 * {@link DeployedOfficeObjectModel} name.
	 */
	private Map<String, String> objectNameMapping;

	/**
	 * Mapping of {@link OfficeInputType} name to
	 * {@link DeployedOfficeInputModel} name.
	 */
	private Map<String, String> inputNameMapping;

	/**
	 * Mapping of {@link OfficeTeamType} name to {@link DeployedOfficeTeamModel}
	 * name.
	 */
	private Map<String, String> teamNameMapping;

	/**
	 * Initiate for public use.
	 *
	 * @param model
	 *            {@link DeployedOfficeModel}.
	 */
	public OfficeInstance(DeployedOfficeModel model) {
		this.officeName = model.getDeployedOfficeName();
		this.officeSourceClassName = model.getOfficeSourceClassName();
		this.officeLocation = model.getOfficeLocation();
		this.deployedOfficeModel = model;
		this.officeType = null;
		this.objectNameMapping = null;
		this.inputNameMapping = null;
		this.teamNameMapping = null;

		// Load the properties
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : model.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}
	}

	/**
	 * Initiate from {@link OfficeSourceInstance}.
	 *
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param officeType
	 *            {@link OfficeType}.
	 * @param objectNameMapping
	 *            Mapping of {@link OfficeManagedObjectType} name to
	 *            {@link DeployedOfficeObjectModel} name.
	 * @param inputNameMapping
	 *            Mapping of {@link OfficeInputType} name to
	 *            {@link DeployedOfficeInputModel} name.
	 * @param teamNameMapping
	 *            Mapping of {@link OfficeTeamType} name to
	 *            {@link DeployedOfficeTeamModel} name.
	 */
	OfficeInstance(String officeName, String officeSourceClassName,
			String officeLocation, PropertyList propertyList,
			OfficeType officeType, Map<String, String> objectNameMapping,
			Map<String, String> inputNameMapping,
			Map<String, String> teamNameMapping) {
		this.officeName = officeName;
		this.officeSourceClassName = officeSourceClassName;
		this.officeLocation = officeLocation;
		this.propertyList = propertyList;
		this.deployedOfficeModel = null;
		this.officeType = officeType;
		this.objectNameMapping = objectNameMapping;
		this.inputNameMapping = inputNameMapping;
		this.teamNameMapping = teamNameMapping;
	}

	/**
	 * Obtains the name of the {@link Office}.
	 *
	 * @return Name of the {@link Office}.
	 */
	public String getOfficeName() {
		return this.officeName;
	}

	/**
	 * Obtains the {@link OfficeSource} class name.
	 *
	 * @return {@link OfficeSource} class name.
	 */
	public String getOfficeSourceClassName() {
		return this.officeSourceClassName;
	}

	/**
	 * Obtains the location of the {@link Office}.
	 *
	 * @return Location of the {@link Office}.
	 */
	public String getOfficeLocation() {
		return this.officeLocation;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 *
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link DeployedOfficeModel}.
	 *
	 * @return {@link DeployedOfficeModel}.
	 */
	public DeployedOfficeModel getDeployedOfficeModel() {
		return this.deployedOfficeModel;
	}

	/**
	 * Obtains the {@link OfficeType}.
	 *
	 * @return {@link OfficeType} if obtained from {@link OfficeSourceInstance}
	 *         or <code>null</code> if initiated by <code>public</code>
	 *         constructor.
	 */
	public OfficeType getOfficeType() {
		return this.officeType;
	}

	/**
	 * Obtains the mapping of {@link OfficeManagedObjectType} name to
	 * {@link DeployedOfficeObjectModel} name.
	 *
	 * @return Mapping of {@link OfficeManagedObjectType} name to
	 *         {@link DeployedOfficeObjectModel} name.
	 */
	public Map<String, String> getObjectNameMapping() {
		return this.objectNameMapping;
	}

	/**
	 * Obtains the mapping of {@link OfficeInputType} name to
	 * {@link DeployedOfficeInputModel} name.
	 *
	 * @return Mapping of {@link OfficeInputType} name to
	 *         {@link DeployedOfficeInputModel} name.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputNameMapping;
	}

	/**
	 * Obtains the mapping of {@link OfficeTeamType} name to
	 * {@link DeployedOfficeTeamModel} name.
	 *
	 * @return Mapping of {@link OfficeTeamType} name to
	 *         {@link DeployedOfficeTeamModel} name.
	 */
	public Map<String, String> getTeamNameMapping() {
		return this.teamNameMapping;
	}
}