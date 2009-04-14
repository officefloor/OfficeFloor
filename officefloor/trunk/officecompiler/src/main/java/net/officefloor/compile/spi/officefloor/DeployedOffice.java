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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Deployed {@link Office} within the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface DeployedOffice {

	/**
	 * Obtains the name of this {@link DeployedOffice}.
	 * 
	 * @return Name of this {@link DeployedOffice}.
	 */
	String getDeployedOfficeName();

	/**
	 * Obtains the {@link DeployedOfficeInput} for the {@link OfficeInputType}.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String inputName);

	/**
	 * Assigns the {@link OfficeFloorTeam} assigned by the
	 * {@link OfficeTeamType}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link OfficeTeamType}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeam}.
	 */
	void assignOfficeTeam(String officeTeamName, OfficeFloorTeam officeFloorTeam);

	/**
	 * Allocates the {@link OfficeFloorManagedObject} required by the
	 * {@link OfficeManagedObjectType}.
	 * 
	 * @param officeManagedObjectName
	 *            Name of the {@link OfficeManagedObjectType}.
	 * @param officeFloorManagedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void allocateManagedObject(String officeManagedObjectName,
			OfficeFloorManagedObject officeFloorManagedObject);

}