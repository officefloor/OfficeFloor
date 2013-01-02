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
package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceProperty;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;

/**
 * Loads the {@link SectionType} from the {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SectionSourceSpecification} for the {@link SectionSource}.
	 * 
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @return {@link PropertyList} of the {@link SectionSourceProperty}
	 *         instances of the {@link SectionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S extends SectionSource> PropertyList loadSpecification(
			Class<S> sectionSourceClass);

	/**
	 * Loads and returns the {@link SectionType} from the {@link SectionSource}.
	 * 
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link SectionType}.
	 * @return {@link SectionType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<S extends SectionSource> SectionType loadSectionType(
			Class<S> sectionSourceClass, String sectionLocation,
			PropertyList propertyList);

	/**
	 * <p>
	 * Loads and returns the {@link OfficeSection} from this
	 * {@link SectionSource}.
	 * <p>
	 * Unlike loading the {@link SectionType} this will recursively load the
	 * {@link SubSection} instances to fully construct the {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeSection}.
	 * @return {@link OfficeSection}.
	 */
	<S extends SectionSource> OfficeSection loadOfficeSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, PropertyList propertyList);

}