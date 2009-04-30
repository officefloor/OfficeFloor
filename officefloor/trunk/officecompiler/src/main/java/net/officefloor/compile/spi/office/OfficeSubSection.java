/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.section.SubSection;

/**
 * {@link SubSection} of an {@link OfficeSection}.
 * 
 * @author Daniel
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

}