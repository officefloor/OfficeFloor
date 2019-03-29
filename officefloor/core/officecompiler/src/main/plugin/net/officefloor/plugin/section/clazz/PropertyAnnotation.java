/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.section.clazz;

/**
 * {@link Property} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyAnnotation {

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value of the property.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param name  Name of the property.
	 * @param value Value of the property.
	 */
	public PropertyAnnotation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Instantiate.
	 * 
	 * @param property {@link Property}.
	 */
	public PropertyAnnotation(Property property) {
		this(property.name(),
				Void.class.equals(property.valueClass()) ? property.valueClass().getName() : property.value());
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
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	public String getValue() {
		return this.value;
	}

}