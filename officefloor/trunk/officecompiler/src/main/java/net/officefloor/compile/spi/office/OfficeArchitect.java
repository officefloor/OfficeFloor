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
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Architect to structure the {@link Office}.
 * 
 * @author Daniel
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
	 * Creates a {@link PropertyList} to be populated with {@link Property}
	 * instances and passed to add an {@link OfficeSection}.
	 * 
	 * @return {@link PropertyList}.
	 */
	PropertyList createPropertyList();

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
	 * Adds an {@link OfficeManagedObject}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addOfficeManagedObject(String managedObjectName,
			String managedObjectSourceClassName);

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

}