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
package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceProperty;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;

/**
 * Loads the {@link SectionType} and {@link OfficeSectionType} from the
 * {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SectionSourceSpecification} for the {@link SectionSource}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @return {@link PropertyList} of the {@link SectionSourceProperty}
	 *         instances of the {@link SectionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S extends SectionSource> PropertyList loadSpecification(Class<S> sectionSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SectionSourceSpecification} for the {@link SectionSource}.
	 * 
	 * @param sectionSource
	 *            {@link SectionSource}.
	 * @return {@link PropertyList} of the {@link SectionSourceProperty}
	 *         instances of the {@link SectionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(SectionSource sectionSource);

	/**
	 * Loads and returns the {@link SectionType} from the {@link SectionSource}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
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
	<S extends SectionSource> SectionType loadSectionType(Class<S> sectionSourceClass, String sectionLocation,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link SectionType} from the {@link SectionSource}.
	 * 
	 * @param sectionSource
	 *            {@link SectionSource} instance.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link SectionType}.
	 * @return {@link SectionType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionType loadSectionType(SectionSource sectionSource, String sectionLocation, PropertyList propertyList);

	/**
	 * <p>
	 * Loads and returns the {@link OfficeSectionType} from this
	 * {@link SectionSource}.
	 * <p>
	 * Unlike loading the {@link SectionType} this will recursively load the
	 * {@link SubSection} instances to fully construct the
	 * {@link OfficeSectionType}.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            Class of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeSectionType}.
	 * @return {@link OfficeSectionType}.
	 */
	<S extends SectionSource> OfficeSectionType loadOfficeSectionType(String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, PropertyList propertyList);

	/**
	 * <p>
	 * Loads and returns the {@link OfficeSectionType} from this
	 * {@link SectionSource}.
	 * <p>
	 * Unlike loading the {@link SectionType} this will recursively load the
	 * {@link SubSection} instances to fully construct the
	 * {@link OfficeSectionType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSource
	 *            {@link SectionSource} instance.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeSectionType}.
	 * @return {@link OfficeSectionType}.
	 */
	OfficeSectionType loadOfficeSectionType(String sectionName, SectionSource sectionSource, String sectionLocation,
			PropertyList propertyList);

}