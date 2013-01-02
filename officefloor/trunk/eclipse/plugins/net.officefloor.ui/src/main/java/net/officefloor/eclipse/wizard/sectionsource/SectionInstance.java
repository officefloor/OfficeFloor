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
package net.officefloor.eclipse.wizard.sectionsource;

import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.PropertyModel;

/**
 * Instance of a {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionInstance {

	/**
	 * Name of this {@link OfficeSection}.
	 */
	private final String sectionName;

	/**
	 * {@link SectionSource} class name.
	 */
	private final String sectionSourceClassName;

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private final String sectionLocation;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeSectionModel}.
	 */
	private final OfficeSectionModel officeSectionModel;

	/**
	 * {@link SectionType}.
	 */
	private final SectionType sectionType;

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection officeSection;

	/**
	 * Mapping of {@link OfficeSectionInput} name to
	 * {@link OfficeSectionInputModel} name.
	 */
	private final Map<String, String> inputNameMapping;

	/**
	 * Mapping of {@link OfficeSectionOutput} name to
	 * {@link OfficeSectionOutputModel} name.
	 */
	private final Map<String, String> outputNameMapping;

	/**
	 * Mapping of {@link OfficeSectionObject} name to
	 * {@link OfficeSectionObjectModel} name.
	 */
	private final Map<String, String> objectNameMapping;

	/**
	 * Initiate for public use.
	 *
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	public SectionInstance(String sectionName, String sectionSourceClassName,
			String sectionLocation) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionLocation = sectionLocation;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.officeSectionModel = null;
		this.sectionType = null;
		this.officeSection = null;
		this.inputNameMapping = null;
		this.outputNameMapping = null;
		this.objectNameMapping = null;
	}

	/**
	 * Initiate for public use from {@link OfficeSectionModel}.
	 *
	 * @param model
	 *            {@link OfficeSectionModel}.
	 */
	public SectionInstance(OfficeSectionModel model) {
		this.sectionName = model.getOfficeSectionName();
		this.sectionSourceClassName = model.getSectionSourceClassName();
		this.sectionLocation = model.getSectionLocation();
		this.officeSectionModel = model;
		this.sectionType = null;
		this.officeSection = null;
		this.inputNameMapping = null;
		this.outputNameMapping = null;
		this.objectNameMapping = null;

		// Load the properties
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : model.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}
	}

	/**
	 * Initiate from {@link SectionSourceInstance}.
	 *
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param sectionType
	 *            {@link SectionType}.
	 * @param officeSection
	 *            {@link OfficeSection}.
	 * @param inputNameMapping
	 *            Mapping of {@link OfficeSectionInput} name to
	 *            {@link OfficeSectionInputModel} name.
	 * @param outputNameMapping
	 *            Mapping of {@link OfficeSectionOutput} name to
	 *            {@link OfficeSectionOutputModel} name.
	 * @param objectNameMapping
	 *            Mapping of {@link OfficeSectionObject} name to
	 *            {@link OfficeSectionObjectModel} name.
	 */
	SectionInstance(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList propertyList,
			SectionType sectionType, OfficeSection officeSection,
			Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping,
			Map<String, String> objectNameMapping) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionLocation = sectionLocation;
		this.propertyList = propertyList;
		this.officeSectionModel = null;
		this.sectionType = sectionType;
		this.officeSection = officeSection;
		this.inputNameMapping = inputNameMapping;
		this.outputNameMapping = outputNameMapping;
		this.objectNameMapping = objectNameMapping;
	}

	/**
	 * Obtains the name of the {@link OfficeSection}.
	 *
	 * @return Name of the {@link OfficeSection}.
	 */
	public String getSectionName() {
		return this.sectionName;
	}

	/**
	 * Obtains the {@link SectionSource} class name.
	 *
	 * @return {@link SectionSource} class name.
	 */
	public String getSectionSourceClassName() {
		return this.sectionSourceClassName;
	}

	/**
	 * Obtains the location of the {@link OfficeSection}.
	 *
	 * @return Location of the {@link OfficeSection}.
	 */
	public String getSectionLocation() {
		return this.sectionLocation;
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
	 * Obtains the {@link OfficeSectionModel}.
	 *
	 * @return {@link OfficeSectionModel}.
	 */
	public OfficeSectionModel getOfficeSectionModel() {
		return this.officeSectionModel;
	}

	/**
	 * Obtains the {@link SectionType}.
	 *
	 * @return {@link SectionType} if obtained from
	 *         {@link SectionSourceInstance} or <code>null</code> if:
	 *         <ol>
	 *         <li>initiated by <code>public</code> constructor</li>
	 *         <li>loading {@link OfficeSection}</li>
	 *         </ol>
	 */
	public SectionType getSectionType() {
		return this.sectionType;
	}

	/**
	 * Obtains the {@link OfficeSection}.
	 *
	 * @return {@link OfficeSection} if obtained from
	 *         {@link SectionSourceInstance} or <code>null</code> if:
	 *         <ol>
	 *         <li>initiated by <code>public</code> constructor</li>
	 *         <li>loading {@link SectionType}</li>
	 *         </ol>
	 */
	public OfficeSection getOfficeSection() {
		return this.officeSection;
	}

	/**
	 * Obtains the input name mapping.
	 *
	 * @return Input name mapping.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputNameMapping;
	}

	/**
	 * Obtains the output name mapping.
	 *
	 * @return Output name mapping.
	 */
	public Map<String, String> getOutputNameMapping() {
		return this.outputNameMapping;
	}

	/**
	 * Obtains the object name mapping.
	 *
	 * @return Object name mapping.
	 */
	public Map<String, String> getObjectNameMapping() {
		return this.objectNameMapping;
	}

}