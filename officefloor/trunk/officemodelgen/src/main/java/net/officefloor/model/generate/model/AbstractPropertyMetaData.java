/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.generate.model;

import net.officefloor.model.generate.GenericMetaData;

/**
 * Abstract property meta-data.
 * 
 * @author Daniel
 */
public abstract class AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public AbstractPropertyMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name
	 *            Name.
	 * @param type
	 *            Type.
	 * @param description
	 *            Description.
	 */
	public AbstractPropertyMetaData(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	/**
	 * Obtains the capitalised name.
	 */
	public String getCapitalisedName() {
		return GenericMetaData.capitalise(this.name);
	}

	/**
	 * Obtains the camel case name.
	 */
	public String getCamelCaseName() {
		return GenericMetaData.camelCase(this.name);
	}

	/**
	 * Obtains the property name.
	 */
	public String getPropertyName() {
		return GenericMetaData.propertyCase(this.name);
	}

	/**
	 * Name.
	 */
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Type.
	 */
	private String type;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Description.
	 */
	private String description;

	public String getDescription() {
		if (this.description == null) {
			// Construct description from name
			return GenericMetaData.titleCase(this.name) + ".";
		} else {
			return this.description;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
