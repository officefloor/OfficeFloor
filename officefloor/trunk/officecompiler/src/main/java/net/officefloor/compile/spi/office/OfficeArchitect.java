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
	 * Obtains an {@link OfficeFloorManagedObject}.
	 * 
	 * @param officeManagedObjectName
	 *            Name of the {@link OfficeFloorManagedObject}.
	 * @param objectType
	 *            Object type.
	 * @return Added {@link OfficeFloorManagedObject}.
	 */
	OfficeFloorManagedObject getOfficeFloorManagedObject(
			String officeManagedObjectName, String objectType);

	/**
	 * Obtains an {@link OfficeTeam}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link OfficeTeam}.
	 * @return Added {@link OfficeTeam}.
	 */
	OfficeTeam getTeam(String officeTeamName);

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
	OfficeSection addSection(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties);

	/**
	 * Adds an {@link OfficeManagedObject}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @return Added {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addManagedObject(String managedObjectName,
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
	OfficeAdministrator addAdministrator(String administratorName,
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
	 * Links the {@link OfficeSectionObject} to be the
	 * {@link OfficeFloorManagedObject}.
	 * 
	 * @param sectionObject
	 *            {@link OfficeSectionObject}.
	 * @param managedObject
	 *            {@link OfficeFloorManagedObject}.
	 */
	void link(OfficeSectionObject sectionObject,
			OfficeFloorManagedObject managedObject);

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

}