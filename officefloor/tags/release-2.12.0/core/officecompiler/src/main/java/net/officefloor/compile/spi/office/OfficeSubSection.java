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
	 * Obtains the {@link OfficeSubSection} instances of this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSubSection} instances.
	 */
	OfficeSubSection[] getOfficeSubSections();

	/**
	 * <p>
	 * Obtains the {@link OfficeTask} instances for this particular
	 * {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeTask} instances of the sub
	 * sections.
	 * 
	 * @return {@link OfficeTask} instances for this particular
	 *         {@link OfficeSubSection}.
	 */
	OfficeTask[] getOfficeTasks();

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionManagedObjectSource} instances for this
	 * particular {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeSectionManagedObjectSource}
	 * instances of the sub sections.
	 * 
	 * @return {@link OfficeSectionManagedObjectSource} instances for this
	 *         particular {@link OfficeSubSection}.
	 */
	OfficeSectionManagedObjectSource[] getOfficeSectionManagedObjectSources();

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link OfficeSubSection}.
	 * <p>
	 * This enables providing {@link Governance} over all {@link OfficeTask}
	 * instances within the {@link OfficeSubSection} and all its subsequent
	 * {@link OfficeSubSection} instances.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	void addGovernance(OfficeGovernance governance);

}