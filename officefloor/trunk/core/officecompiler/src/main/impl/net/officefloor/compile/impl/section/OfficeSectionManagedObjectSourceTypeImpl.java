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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;

/**
 * {@link OfficeSectionManagedObjectSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionManagedObjectSourceTypeImpl implements
		OfficeSectionManagedObjectSourceType {

	/**
	 * Name of the {@link OfficeSectionManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link OfficeSectionManagedObjectTeamType} instances for the
	 * {@link ManagedObjectTeam} instances of the
	 * {@link OfficeSectionManagedObjectSource}.
	 */
	private final OfficeSectionManagedObjectTeamType[] teamTypes;

	/**
	 * {@link OfficeSectionManagedObjectType} instances for the
	 * {@link OfficeSectionManagedObjectSource}.
	 */
	private final OfficeSectionManagedObjectType[] managedObjectTypes;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeSectionManagedObjectSource}.
	 * @param teamTypes
	 *            {@link OfficeSectionManagedObjectTeamType} instances for the
	 *            {@link ManagedObjectTeam} instances of the
	 *            {@link OfficeSectionManagedObjectSource}.
	 * @param managedObjectTypes
	 *            {@link OfficeSectionManagedObjectType} instances for the
	 *            {@link OfficeSectionManagedObjectSource}.
	 */
	public OfficeSectionManagedObjectSourceTypeImpl(
			String managedObjectSourceName,
			OfficeSectionManagedObjectTeamType[] teamTypes,
			OfficeSectionManagedObjectType[] managedObjectTypes) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.teamTypes = teamTypes;
		this.managedObjectTypes = managedObjectTypes;
	}

	/*
	 * ================== OfficeSectionManagedObjectSourceType ===============
	 */

	@Override
	public String getOfficeSectionManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public OfficeSectionManagedObjectTeamType[] getOfficeSectionManagedObjectTeamTypes() {
		return this.teamTypes;
	}

	@Override
	public OfficeSectionManagedObjectType[] getOfficeSectionManagedObjectTypes() {
		return this.managedObjectTypes;
	}

}