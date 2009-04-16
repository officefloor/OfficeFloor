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
package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> of an input into the {@link Office} that may be
 * used for a {@link ManagedObjectFlow} added to the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeInputType {

	/**
	 * Obtains the name of the {@link OfficeSection} containing the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @return Name of the {@link OfficeSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the name of {@link DeployedOfficeInput} on the
	 * {@link OfficeSection}.
	 * 
	 * @return Name of this {@link DeployedOfficeInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Obtains the fully qualified class name of the parameter type to this
	 * {@link OfficeInputType}.
	 * 
	 * @return Parameter type to this {@link OfficeInputType}.
	 */
	String getParameterType();

}