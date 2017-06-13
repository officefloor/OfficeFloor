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

import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Architect to structure the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeArchitect {

	/**
	 * Flags to attempt to auto wire any non-configured object links.
	 */
	void enableAutoWireObjects();

	/**
	 * Flags to attempt to auto wire any non-configured {@link Team} links.
	 */
	void enableAutoWireTeams();

	/**
	 * Adds a {@link OfficeInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeInput}.
	 * @param parameterType
	 *            Fully qualified type name of the parameter to this
	 *            {@link OfficeInput}.
	 * @return Added {@link OfficeInput}.
	 */
	OfficeInput addOfficeInput(String inputName, String parameterType);

	/**
	 * Adds a {@link OfficeOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeOutput}.
	 * @param argumentType
	 *            Fully qualified type name of the argument from this
	 *            {@link OfficeOutput}.
	 * @return Added {@link OfficeOutput}.
	 */
	OfficeOutput addOfficeOutput(String outputName, String argumentType);

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
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName, String sectionSourceClassName, String sectionLocation);

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSource
	 *            {@link SectionSource} instance to use.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName, SectionSource sectionSource, String sectionLocation);

	/**
	 * Adds an {@link OfficeSectionTransformer} to transform the
	 * {@link OfficeSection} instances of the {@link Office}.
	 * 
	 * @param transformer
	 *            {@link OfficeSectionTransformer}.
	 */
	void addOfficeSectionTransformer(OfficeSectionTransformer transformer);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds an {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName
	 *            Name of the {@link OfficeManagedObjectPool}.
	 * @param managedObjectPoolSourceClassName
	 *            Fully qualified class name of the
	 *            {@link ManagedObjectPoolSource}.
	 * @return Added {@link OfficeManagedObjectPool}.
	 */
	OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName, String managedObjectPoolSourceClassName);

	/**
	 * Adds an {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName
	 *            Name of the {@link OfficeManagedObjectPool}.
	 * @param managedObjectPoolSource
	 *            {@link ManagedObjectPoolSource} instance to use.
	 * @return {@link OfficeManagedObjectPool}.
	 */
	OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Adds an {@link OfficeSupplier}.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeSupplier}.
	 * @param supplierSourceClassName
	 *            Fully qualified class name of the {@link SupplierSource}.
	 * @return {@link OfficeSupplier}.
	 */
	OfficeSupplier addSupplier(String supplierName, String supplierSourceClassName);

	/**
	 * Adds an {@link OfficeSupplier}.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeSupplier}.
	 * @param supplierSource
	 *            {@link SupplierSource} instance to use.
	 * @return {@link OfficeFloorSupplier}.
	 */
	OfficeSupplier addSupplier(String supplierName, SupplierSource supplierSource);

	/**
	 * Adds a {@link OfficeGovernance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link OfficeGovernance}.
	 * @param governanceSourceClassName
	 *            Fully qualified class name of the {@link GovernanceSource}.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName, String governanceSourceClassName);

	/**
	 * Adds an {@link OfficeGovernance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link OfficeGovernance}.
	 * @param governanceSource
	 *            {@link GovernanceSource} instance to use.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName, GovernanceSource<?, ?> governanceSource);

	/**
	 * Adds an {@link OfficeAdministration}.
	 * 
	 * @param administrationName
	 *            Name of the {@link OfficeAdministration}.
	 * @param administrationSourceClassName
	 *            Fully qualified class name of the
	 *            {@link AdministrationSource}.
	 * @return Added {@link OfficeAdministration}.
	 */
	OfficeAdministration addOfficeAdministration(String administrationName, String administrationSourceClassName);

	/**
	 * Adds an {@link OfficeAdministration}.
	 * 
	 * @param administrationName
	 *            Name of the {@link OfficeAdministration}.
	 * @param administrationSource
	 *            {@link AdministrationSource} instance to use.
	 * @return Added {@link OfficeAdministration}.
	 */
	OfficeAdministration addOfficeAdministration(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource);

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
	 * Links the {@link OfficeOutput} for synchronous response to an
	 * {@link OfficeInput}.
	 * 
	 * @param input
	 *            {@link OfficeInput} to receive request.
	 * @param output
	 *            {@link OfficeOutput} to provide response.
	 */
	@Deprecated // integration via queues so no synchronous communication
	void link(OfficeInput input, OfficeOutput output);

	/**
	 * Links the {@link OfficeInput} to be handled by the
	 * {@link OfficeSectionInput}.
	 * 
	 * @param input
	 *            {@link OfficeInput}.
	 * @param sectionInput
	 *            {@link OfficeSectionInput}.
	 */
	void link(OfficeInput input, OfficeSectionInput sectionInput);

	/**
	 * Links the {@link OfficeInput} for synchronous request to an
	 * {@link OfficeOutput}.
	 * 
	 * @param output
	 *            {@link OfficeOutput} to make request.
	 * @param input
	 *            {@link OfficeInput} to handle response.
	 */
	@Deprecated // integration via queues so no synchronous communication
	void link(OfficeOutput output, OfficeInput input);

	/**
	 * Links the {@link OfficeSectionOutput} to be handled by the
	 * {@link OfficeOutput}.
	 * 
	 * @param sectionOutput
	 *            {@link OfficeSectionOutput}.
	 * @param output
	 *            {@link OfficeOutput}.
	 */
	void link(OfficeSectionOutput sectionOutput, OfficeOutput output);

	/**
	 * Links the {@link OfficeSectionObject} to be the
	 * {@link OfficeManagedObject}.
	 * 
	 * @param sectionObject
	 *            {@link OfficeSectionObject}.
	 * @param managedObject
	 *            {@link OfficeManagedObject}.
	 */
	void link(OfficeSectionObject sectionObject, OfficeManagedObject managedObject);

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
	 * Links the {@link OfficeManagedObjectSource} to be pooled by the
	 * {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link OfficeManagedObjectPool}.
	 */
	void link(OfficeManagedObjectSource managedObjectSource, OfficeManagedObjectPool managedObjectPool);

	/**
	 * Links the {@link ManagedObjectDependency} to be the
	 * {@link OfficeManagedObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param managedObject
	 *            {@link OfficeManagedObject}.
	 */
	void link(ManagedObjectDependency dependency, OfficeManagedObject managedObject);

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
	 * Links the {@link ResponsibleTeam} to be the {@link OfficeTeam}.
	 * 
	 * @param team
	 *            {@link ResponsibleTeam}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(ResponsibleTeam team, OfficeTeam officeTeam);

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
	 * {@link OfficeAdministration}.
	 * 
	 * @param administrator
	 *            {@link OfficeAdministration}.
	 * @param officeTeam
	 *            {@link OfficeTeam}.
	 */
	void link(OfficeAdministration administrator, OfficeTeam officeTeam);

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
	 */
	void addIssue(String issueDescription);

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
	 */
	void addIssue(String issueDescription, Throwable cause);

}