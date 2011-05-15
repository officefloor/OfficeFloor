/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.properties.Property;

/**
 * {@link SubSection} of a {@link Section}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSection {

	/**
	 * Obtains the name of this {@link SubSection}.
	 * 
	 * @return Name of this {@link SubSection}.
	 */
	String getSubSectionName();

	/**
	 * Adds a {@link Property} to source this {@link SubSection}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	void addProperty(String name, String value);

	/**
	 * Obtains the {@link SubSectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} to obtain.
	 * @return {@link SubSectionInput}.
	 */
	SubSectionInput getSubSectionInput(String inputName);

	/**
	 * Obtains the {@link SubSectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link SubSectionOutput} to obtain.
	 * @return {@link SubSectionOutput}.
	 */
	SubSectionOutput getSubSectionOutput(String outputName);

	/**
	 * Obtains the {@link SubSectionObject}.
	 * 
	 * @param objectName
	 *            Name of the {@link SubSectionObject} to obtain.
	 * @return {@link SubSectionObject}.
	 */
	SubSectionObject getSubSectionObject(String objectName);

}