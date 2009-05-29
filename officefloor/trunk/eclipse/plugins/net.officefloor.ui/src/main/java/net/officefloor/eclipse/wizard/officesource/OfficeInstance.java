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
package net.officefloor.eclipse.wizard.officesource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.manage.Office;

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
	 * {@link OfficeType}.
	 */
	private final OfficeType officeType;

	/**
	 * Initiate for public use.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	public OfficeInstance(String officeName, String officeSourceClassName,
			String officeLocation) {
		this.officeName = officeName;
		this.officeSourceClassName = officeSourceClassName;
		this.officeLocation = officeLocation;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.officeType = null;
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
	 */
	OfficeInstance(String officeName, String officeSourceClassName,
			String officeLocation, PropertyList propertyList,
			OfficeType officeType) {
		this.officeName = officeName;
		this.officeSourceClassName = officeSourceClassName;
		this.officeLocation = officeLocation;
		this.propertyList = propertyList;
		this.officeType = officeType;
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
	 * Obtains the {@link OfficeType}.
	 * 
	 * @return {@link OfficeType} if obtained from {@link OfficeSourceInstance}
	 *         or <code>null</code> if initiated by <code>public</code>
	 *         constructor.
	 */
	public OfficeType getOfficeType() {
		return this.officeType;
	}

}