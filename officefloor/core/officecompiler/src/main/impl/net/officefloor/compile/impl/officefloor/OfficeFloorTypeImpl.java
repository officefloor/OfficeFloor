/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.officefloor.OfficeFloorType;

/**
 * {@link OfficeFloorType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTypeImpl implements OfficeFloorType {

	/**
	 * {@link OfficeFloorPropertyType} instances.
	 */
	private final OfficeFloorPropertyType[] propertyTypes;

	/**
	 * {@link OfficeFloorManagedObjectSourceType} instances.
	 */
	private final OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes;

	/**
	 * {@link OfficeFloorTeamSourceType} instances.
	 */
	private final OfficeFloorTeamSourceType[] teamSourceTypes;

	/**
	 * Initialise.
	 * 
	 * @param propertyTypes
	 *            {@link OfficeFloorPropertyType} instances.
	 * @param managedObjectSourceTypes
	 *            {@link OfficeFloorManagedObjectSourceType} instances.
	 * @param teamSourceTypes
	 *            {@link OfficeFloorTeamSourceType} instances.
	 */
	public OfficeFloorTypeImpl(OfficeFloorPropertyType[] propertyTypes,
			OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes,
			OfficeFloorTeamSourceType[] teamSourceTypes) {
		this.propertyTypes = propertyTypes;
		this.managedObjectSourceTypes = managedObjectSourceTypes;
		this.teamSourceTypes = teamSourceTypes;
	}

	/*
	 * ====================== OfficeFloorType ============================
	 */

	@Override
	public OfficeFloorPropertyType[] getOfficeFloorPropertyTypes() {
		return this.propertyTypes;
	}

	@Override
	public OfficeFloorManagedObjectSourceType[] getOfficeFloorManagedObjectSourceTypes() {
		return this.managedObjectSourceTypes;
	}

	@Override
	public OfficeFloorTeamSourceType[] getOfficeFloorTeamSourceTypes() {
		return this.teamSourceTypes;
	}

}
