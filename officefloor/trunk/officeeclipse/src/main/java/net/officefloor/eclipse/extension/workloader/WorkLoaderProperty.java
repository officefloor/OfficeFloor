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
package net.officefloor.eclipse.extension.workloader;

import net.officefloor.work.WorkLoader;

/**
 * Property for the {@link WorkLoader}.
 * 
 * @author Daniel
 */
public class WorkLoaderProperty {

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value for the property.
	 */
	private String value;

	/**
	 * Initiate with name and value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value of the property.
	 */
	public WorkLoaderProperty(String name, String value) {
		this.name = name;
		this.setValue(value);
	}

	/**
	 * Initiate with name and no value.
	 * 
	 * @param name
	 *            Name of the property.
	 */
	public WorkLoaderProperty(String name) {
		this(name, null);
	}

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the value for the property.
	 * 
	 * @return Value for the property.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Specifies the vale of the property.
	 * 
	 * @param value
	 *            Value of the property.
	 */
	public void setValue(String value) {
		this.value = (value == null ? "" : value);
	}
}
