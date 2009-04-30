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

import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Deploys the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorDeployer {

	/**
	 * Adds a {@link Team}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClassName
	 *            Fully qualified class name of the {@link TeamSource}.
	 * @return Added {@link OfficeFloorTeam}.
	 */
	OfficeFloorTeam addTeam(String teamName, String teamSourceClassName);

	/**
	 * Adds a {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName);

	/**
	 * Adds a {@link DeployedOffice} to the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeSourceClassName
	 *            Fully qualified class name of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @return Deployed {@link DeployedOffice}.
	 */
	DeployedOffice addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation);

	/**
	 * Links the {@link ManagedObjectTeam} to be the {@link OfficeFloorTeam}.
	 * 
	 * @param team
	 *            {@link ManagedObjectTeam}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeam}.
	 */
	void link(ManagedObjectTeam team, OfficeFloorTeam officeFloorTeam);

	/**
	 * Links the {@link ManagedObjectDependency} to be the
	 * {@link OfficeFloorManagedObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param managedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void link(ManagedObjectDependency dependency,
			OfficeFloorManagedObject managedObject);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param flow
	 *            {@link ManagedObjectFlow}.
	 * @param input
	 *            {@link DeployedOfficeInput}.
	 */
	void link(ManagedObjectFlow flow, DeployedOfficeInput input);

	/**
	 * Links the {@link ManagingOffice} to be managed by the
	 * {@link DeployedOffice}.
	 * 
	 * @param managingOffice
	 *            {@link ManagingOffice}.
	 * @param office
	 *            {@link DeployedOffice}.
	 */
	void link(ManagingOffice managingOffice, DeployedOffice office);

	/**
	 * Links the {@link OfficeTeam} to be the {@link OfficeFloorTeam}.
	 * 
	 * @param team
	 *            {@link OfficeTeam}.
	 * @param officeFloorTeam
	 *            {@link OfficeFloorTeam}.
	 */
	void link(OfficeTeam team, OfficeFloorTeam officeFloorTeam);

	/**
	 * Links {@link OfficeObject} to be the {@link OfficeFloorManagedObject}.
	 * 
	 * @param officeObject
	 *            {@link OfficeObject}.
	 * @param officeFloorManagedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void link(OfficeObject officeObject,
			OfficeFloorManagedObject officeFloorManagedObject);

	/**
	 * <p>
	 * Allows the {@link OfficeFloorSource} to add an issue in attempting to
	 * deploy the {@link OfficeFloor}.
	 * <p>
	 * This is available to report invalid configuration but continue to deploy
	 * the rest of the {@link OfficeFloor}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if
	 *            {@link OfficeFloor} in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link OfficeFloor} in general.
	 */
	void addIssue(String issueDescription, AssetType assetType, String assetName);

	/**
	 * <p>
	 * Allows the {@link OfficeFloorSource} to add an issue along with its cause
	 * in attempting to deploy the {@link OfficeFloor}.
	 * <p>
	 * This is available to report invalid configuration but continue to deploy
	 * the rest of the {@link OfficeFloor}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if
	 *            {@link OfficeFloor} in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link OfficeFloor} in general.
	 */
	void addIssue(String issueDescription, Throwable cause,
			AssetType assetType, String assetName);

}