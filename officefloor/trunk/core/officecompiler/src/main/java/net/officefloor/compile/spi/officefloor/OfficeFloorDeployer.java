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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Deploys the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
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
	 * Adds an {@link OfficeFloorManagedObjectSource}.
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
	 * Adds an {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return Added {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds an {@link OfficeFloorInputManagedObject}.
	 * 
	 * @param inputManagedObjectName
	 *            Name of the {@link OfficeFloorInputManagedObject}.
	 * @return Added {@link OfficeFloorInputManagedObject}.
	 */
	OfficeFloorInputManagedObject addInputManagedObject(
			String inputManagedObjectName);

	/**
	 * <p>
	 * Adds an {@link OfficeFloorSupplier}.
	 * <p>
	 * Please note there is no {@link AutoWire} functionality and this is only
	 * provided to allow {@link OfficeFloorSource} implementations to take
	 * advantage of a {@link SupplierSource}.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier}.
	 * @param supplierSourceClassName
	 *            Fully qualified class name of the {@link SupplierSource}.
	 * @return {@link OfficeFloorSupplier}.
	 * 
	 * @see AutoWireApplication
	 */
	OfficeFloorSupplier addSupplier(String supplierName,
			String supplierSourceClassName);

	/**
	 * Adds a {@link DeployedOffice} to the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeSourceClassName
	 *            Fully qualified class name of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation);

	/**
	 * Adds a {@link DeployedOffice} to the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeSource
	 *            {@link OfficeSource} instance.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice addDeployedOffice(String officeName,
			OfficeSource officeSource, String officeLocation);

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
	 * Links the {@link OfficeFloorInputManagedObject} to be input by the
	 * {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObject}.
	 */
	void link(OfficeFloorManagedObjectSource managedObjectSource,
			OfficeFloorInputManagedObject inputManagedObject);

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
	 * Links the {@link ManagedObjectDependency} to be the
	 * {@link OfficeFloorInputManagedObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObject}.
	 */
	void link(ManagedObjectDependency dependency,
			OfficeFloorInputManagedObject inputManagedObject);

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
	 * Links the {@link OfficeObject} to be the {@link OfficeFloorManagedObject}
	 * .
	 * 
	 * @param officeObject
	 *            {@link OfficeObject}.
	 * @param managedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void link(OfficeObject officeObject, OfficeFloorManagedObject managedObject);

	/**
	 * Links the {@link OfficeObject} to be the
	 * {@link OfficeFloorInputManagedObject}.
	 * 
	 * @param officeObject
	 *            {@link OfficeObject}.
	 * @param inputManagedObject
	 *            {@link OfficeFloorInputManagedObject}.
	 */
	void link(OfficeObject officeObject,
			OfficeFloorInputManagedObject inputManagedObject);

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
	 */
	void addIssue(String issueDescription);

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
	 */
	void addIssue(String issueDescription, Throwable cause);

}