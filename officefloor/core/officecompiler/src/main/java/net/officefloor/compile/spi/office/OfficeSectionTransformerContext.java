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
	 * Obtains the {@link OfficeSection} to transform.
	 * 
	 * @return {@link OfficeSection} to transform.
	 */
	OfficeSection getSection();

	/**
	 * Obtains the {@link SectionSource} {@link Class} of the
	 * {@link OfficeSection} being transformed.
	 * 
	 * @return {@link SectionSource} {@link Class} of the {@link OfficeSection}
	 *         being transformed.
	 */
	<S extends SectionSource> Class<S> getSectionSourceClass();

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
	 * Creates an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} {@link Class} name.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @return New {@link OfficeSection}.
	 */
	OfficeSection createSection(String sectionName, String sectionSourceClassName, String sectionLocation);

}