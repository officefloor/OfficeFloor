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
package net.officefloor.compile.spi.office.source;

import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSection} of the {@link Office}.
 * 
 * @author Daniel
 */
public interface OfficeSection {

	/**
	 * Obtains the name of this {@link OfficeSection}.
	 * 
	 * @return Name of this {@link OfficeSection}.
	 */
	String getSectionName();

	/**
	 * Obtains the {@link OfficeSection} instances that are sub sections of this
	 * {@link OfficeSection}.
	 * 
	 * @return Sub section {@link OfficeSection} instances.
	 */
	OfficeSection[] getSubSections();

	/**
	 * <p>
	 * Obtains the {@link OfficeTask} instances for this particular
	 * {@link OfficeSection}.
	 * <p>
	 * This does not include the {@link OfficeTask} instances of the sub
	 * sections.
	 * 
	 * @return {@link OfficeTask} instances for this particular
	 *         {@link OfficeSection}.
	 */
	OfficeTask[] getTasks();

}