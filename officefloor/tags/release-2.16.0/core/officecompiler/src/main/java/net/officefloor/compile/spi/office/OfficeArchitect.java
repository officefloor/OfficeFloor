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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Architect to structure the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeArchitect {

	/**
	 * Adds an {@link OfficeObject}.
	 * 
	 * @param officeObjectName
	 *            Name of the {@link OfficeObject}.
	 * @param objectType
	 *            Object type.
	 * @return Added {@link OfficeObject}.
	 */
	OfficeObject addOfficeObject(String officeObjectName, String objectType);

	/**
	 * Adds an {@link OfficeTeam}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link OfficeTeam}.
	 * @return Added {@link OfficeTeam}.
	 */
	OfficeTeam addOfficeTeam(String officeTeamName);

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            Fully qualified class name of the {@link SectionSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList} to load the {@link OfficeSection}.
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties);

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSource
	 *            {@link SectionSource} instance to use.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param properties
	 *            {@link PropertyList} to load the {@link OfficeSection}.
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName,
			SectionSource sectionSource, String sectionLocation,
			PropertyList properties);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds a {@link OfficeGovernance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link OfficeGovernance}.
	 * @param governanceSourceClassName
	 *            Fully qualified class name of the {@link GovernanceSource}.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName,
			String governanceSourceClassName);

	/**
	 * Adds an {@link OfficeGovernance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link OfficeGovernance}.
	 * @param governanceSource
	 *            {@link GovernanceSource} instance to use.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName,
			GovernanceSource<?, ?> governanceSource);

	/**
	 * Adds an {@link OfficeAdministrator}.
	 * 
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 * @param administratorSourceClassName
	 *            Fully qualified class name of the {@link AdministratorSource}.
	 * @return Added {@link OfficeAdministrator}.
	 */
	OfficeAdministrator addOfficeAdministrator(String administratorName,
			String administratorSourceClassName);

	/**
	 * Adds an {@link OfficeAdministrator}.
	 * 
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 * @param administratorSource
	 *            {@link AdministratorSource} instance to use.
	 * @return Added {@link OfficeAdministrator}.
	 */
	OfficeAdministrator addOfficeAdministrator(String administratorName,
			AdministratorSource<?, ?> administratorSource);

	/**
	 * Adds an {@link OfficeEscalation}.
	 * 
	 * @param escalationTypeName
	 *            Type of {@link Escalation}.
	 * @return Added {@link OfficeEscalation}.
	 */
	OfficeEscalation addOfficeEscalation(String escalationTypeName);

	/**
	 * Adds an {@link OfficeStart}.
	 * 
	 * @param startName
	 *            Name of the {@link OfficeStart}.
	 * @return Added {@link OfficeStart}.
	 */
	OfficeStart addOfficeStart(String startName);

	/**
	 * Links the {@link OfficeSectionObject} to be the
	 * {@link OfficeManagedObject}.
	 * 
	 * @param sectionObject
	 *            {@link OfficeSectionObject}.
	 * @param managedObject
	 *            {@link OfficeManagedObject}.
	 */
	void link(OfficeSectionObject sectionObject,
			OfficeManagedObject managedObject);

	/**
	 * Links the {@link OfficeSectionObject} to be the {@link OfficeObject}.
	 * 
	 * @param sectionObject
	 *            {@link OfficeSectionObject}.
	 * @param managedObject
	 *            {@link OfficeObject}.
	 */
	void link(OfficeSectionObject sectionObject, OfficeObject managedObject);

	/**
	 * Links the {@link ManagedObjectDependency} to be the
	 * {@link OfficeManagedObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param managedObject
	 *            {@link OfficeManagedObject}.
	 */
	void link(ManagedObjectDependency dependency,
			OfficeManagedObject managedObject);

	/**
	 * Links the {@link ManagedObjectDependency} to be the {@link OfficeObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param managedObject
	 *            {@link OfficeObject}.
	 */
	void link(ManagedObjectDependency dependency, OfficeObject managedObject);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link OfficeSectionInput}.
	 * 
	 * @param flow
	 *            {@link ManagedObjectFlow}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 */
	void link(ManagedObjectFlow flow, OfficeSectionInput input);

	/**
	 * Links the {@link OfficeSectionObject} to be undertaken by the
	 * {@link OfficeSectionInput}.
	 * 
	 * @param output
	 *            {@link OfficeSectionOutput}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 */
	void link(OfficeSectionOutput output, OfficeSectionInput input);

	/**
	 * Links the {@link OfficeEscalation} to be undertaken by the
	 * {@link OfficeSectionInput}.
	 * 
	 * @param escalation
	 *            {@link OfficeEscalation}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 */
	void link(OfficeEscalation escalation, OfficeSectionInput input);

	/**
	 * Links the {@link OfficeStart} to trigger the {@link OfficeSectionInput}.
	 * 
	 * @param start
	 *            {@link OfficeStart}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 */
	void link(OfficeStart start, OfficeSectionInput input);

	/**
	 * Links the {@link TaskTeam} to be the {@link OfficeTeam}.
	 * 
	 * @param team
	 *            {@link TaskTeam}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(TaskTeam team, OfficeTeam officeTeam);

	/**
	 * Links the {@link ManagedObjectTeam} to be the {@link OfficeTeam}.
	 * 
	 * @param team
	 *            {@link ManagedObjectTeam}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(ManagedObjectTeam team, OfficeTeam officeTeam);

	/**
	 * Links the {@link OfficeTeam} to be responsible for the
	 * {@link OfficeGovernance}.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(OfficeGovernance governance, OfficeTeam officeTeam);

	/**
	 * Links the {@link OfficeTeam} to be responsible for the
	 * {@link OfficeAdministrator}.
	 * 
	 * @param administrator
	 *            {@link OfficeAdministrator}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(OfficeAdministrator administrator, OfficeTeam officeTeam);

	/**
	 * <p>
	 * Allows the {@link OfficeSource} to add an issue in attempting to
	 * architect the {@link Office}.
	 * <p>
	 * This is available to report invalid configuration but continue to
	 * architect the rest of the {@link Office}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if {@link Office}
	 *            in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link Office} in general.
	 */
	void addIssue(String issueDescription, AssetType assetType, String assetName);

	/**
	 * <p>
	 * Allows the {@link OfficeSource} to add an issue along with its cause in
	 * attempting to architect the {@link Office}.
	 * <p>
	 * This is available to report invalid configuration but continue to
	 * architect the rest of the {@link Office}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if {@link Office}
	 *            in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link Office} in general.
	 */
	void addIssue(String issueDescription, Throwable cause,
			AssetType assetType, String assetName);

}