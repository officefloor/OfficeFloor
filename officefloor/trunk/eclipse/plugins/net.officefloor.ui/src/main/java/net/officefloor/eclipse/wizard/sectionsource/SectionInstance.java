/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;

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
	 * {@link SectionType}.
	 */
	private final SectionType sectionType;

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection officeSection;

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
		this.sectionType = null;
		this.officeSection = null;
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
	 */
	SectionInstance(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList propertyList,
			SectionType sectionType, OfficeSection officeSection) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionLocation = sectionLocation;
		this.propertyList = propertyList;
		this.sectionType = sectionType;
		this.officeSection = officeSection;
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

}