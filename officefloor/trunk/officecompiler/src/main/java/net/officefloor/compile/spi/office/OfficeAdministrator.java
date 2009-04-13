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

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * {@link Administrator} within the {@link Office}.
 * 
 * @author Daniel
 */
public interface OfficeAdministrator {

	/**
	 * Obtains the name of this {@link OfficeAdministrator}.
	 * 
	 * @return Name of this {@link OfficeAdministrator}.
	 */
	String getOfficeAdministratorName();

	/**
	 * Adds a {@link Property} to source the {@link Administrator} from the
	 * {@link AdministratorSource}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	void addProperty(String name, String value);

	OfficeDuty getDuty(String dutyName);

	void administerManagedObject(OfficeManagedObject managedObject);

	void administerManagedObject(OfficeSectionManagedObject managedObject);

	void administerManagedObject(OfficeFloorManagedObject managedObject);

}