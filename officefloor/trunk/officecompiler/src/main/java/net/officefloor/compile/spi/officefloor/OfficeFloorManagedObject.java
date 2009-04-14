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

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObject} within the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorManagedObject {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObject}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObject}.
	 */
	String getOfficeFloorManagedObjectName();

	/**
	 * Specifies the {@link DeployedOffice} that is responsible for managing
	 * this {@link OfficeFloorManagedObject}.
	 * 
	 * @param office
	 *            Managing {@link DeployedOffice}.
	 */
	void setManagingOffice(DeployedOffice office);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param managedObjectFlowName
	 *            Name of the {@link ManagedObjectFlowType}.
	 * @param input
	 *            {@link DeployedOfficeInput}.
	 */
	void linkManagedObjectFlow(String managedObjectFlowName,
			DeployedOfficeInput input);

	/**
	 * Allocates the {@link OfficeFloorManagedObject} that full fills the
	 * dependency.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @param managedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void allocateManagedObjectDependency(String managedObjectDependencyName,
			OfficeFloorManagedObject managedObject);

	/**
	 * Assigns the {@link OfficeFloorTeam} required by the
	 * {@link OfficeFloorManagedObject}.
	 * 
	 * @param managedObjectTeamName
	 *            Name of the {@link ManagedObjectTeamType}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeam}.
	 */
	void assignManagedObjectTeam(String managedObjectTeamName,
			OfficeFloorTeam officeFloorTeam);

}