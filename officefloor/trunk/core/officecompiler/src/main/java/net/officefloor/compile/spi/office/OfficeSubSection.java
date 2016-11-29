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

import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link SubSection} of an {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSubSection {

	/**
	 * Obtains the name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 * 
	 * @return Name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the {@link OfficeSubSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSubSection} to obtain.
	 * @return {@link OfficeSubSection}.
	 */
	OfficeSubSection getOfficeSubSection(String sectionName);

	/**
	 * Obtains the {@link OfficeSectionTask} instance particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param taskName
	 *            Name of the {@link OfficeSectionTask} to obtain.
	 * @return {@link OfficeSectionTask}.
	 */
	OfficeSectionTask getOfficeSectionTask(String taskName);

	/**
	 * Obtains the {@link OfficeSectionManagedObject} particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeSectionManagedObject} to obtain.
	 * @return {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObject getOfficeSectionManagedObject(
			String managedObjectName);

	/**
	 * Obtains the {@link OfficeSectionManagedObjectSource} particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeSectionManagedObjectSource} to
	 *            obtain.
	 * @return {@link OfficeSectionManagedObjectSource}.
	 */
	OfficeSectionManagedObjectSource getOfficeSectionManagedObjectSource(
			String managedObjectSourceName);

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link OfficeSubSection}.
	 * <p>
	 * This enables providing {@link Governance} over all {@link OfficeSectionTask}
	 * instances within the {@link OfficeSubSection} and all its subsequent
	 * {@link OfficeSubSection} instances.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	void addGovernance(OfficeGovernance governance);

}