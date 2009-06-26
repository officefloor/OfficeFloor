/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.compile.properties;

/**
 * Property.
 * 
 * @author Daniel Sagenschneider
 */
public interface Property {

	/**
	 * Obtains the display label for the property.
	 * 
	 * @return Display label for the property.
	 */
	String getLabel();

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	String getName();

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	String getValue();

	/**
	 * Changes the value of the property.
	 * 
	 * @param value
	 *            Value of the property.
	 */
	void setValue(String value);

}