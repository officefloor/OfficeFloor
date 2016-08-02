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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSubSection;

/**
 * <code>Type definition</code> of a section of the {@link OfficeSubSection}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSubSectionType {

	/**
	 * Obtains the name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 * 
	 * @return Name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the parent {@link OfficeSubSectionType}.
	 * 
	 * @return Parent {@link OfficeSubSectionType} or <code>null</code> if this
	 *         is the {@link OfficeSectionType}.
	 */
	OfficeSubSectionType getParentOfficeSubSectionType();

	/**
	 * Obtains the {@link OfficeSubSectionType} instances of this
	 * {@link OfficeSectionType}.
	 * 
	 * @return {@link OfficeSubSectionType} instances.
	 */
	OfficeSubSectionType[] getOfficeSubSectionTypes();

	/**
	 * <p>
	 * Obtains the {@link OfficeTaskType} instances for this particular
	 * {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeTaskType} instances of the sub
	 * sections.
	 * 
	 * @return {@link OfficeTaskType} instances for this particular
	 *         {@link OfficeSubSection}.
	 */
	OfficeTaskType[] getOfficeTaskTypes();

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionManagedObjectSourceType} instances for
	 * this particular {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeSectionManagedObjectSourceType}
	 * instances of the sub sections.
	 * 
	 * @return {@link OfficeSectionManagedObjectSourceType} instances for this
	 *         particular {@link OfficeSubSection}.
	 */
	OfficeSectionManagedObjectSourceType[] getOfficeSectionManagedObjectSourceTypes();

}