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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Context for the {@link OfficeSectionTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionTransformerContext {

	/**
	 * Obtains the {@link SectionSource} {@link Class} name of the
	 * {@link OfficeSection} being transformed.
	 * 
	 * @return {@link SectionSource} {@link Class} name of the
	 *         {@link OfficeSection} being transformed.
	 */
	String getSectionSourceClassName();

	/**
	 * Obtains the location of the {@link OfficeSection} being transformed.
	 * 
	 * @return Location of the {@link OfficeSection} being transformed.
	 */
	String getSectionLocation();

	/**
	 * Obtains the {@link PropertyList} of the {@link OfficeSection} being
	 * transformed.
	 * 
	 * @return {@link PropertyList} of the {@link OfficeSection} being
	 *         transformed.
	 */
	PropertyList getSectionProperties();

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * Creates an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} {@link Class} name.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @param sectionProperties
	 *            {@link OfficeSection} {@link PropertyList}.
	 */
	void setTransformedOfficeSection(String sectionSourceClassName, String sectionLocation,
			PropertyList sectionProperties);

}