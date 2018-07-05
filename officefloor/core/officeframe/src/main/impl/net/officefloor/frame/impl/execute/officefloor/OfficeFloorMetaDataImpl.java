/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link OfficeFloorMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMetaDataImpl implements OfficeFloorMetaData {

	/**
	 * Listing of {@link TeamManagement} instances.
	 */
	private final TeamManagement[] teams;

	/**
	 * Listing of {@link ManagedObjectSourceInstance} instances.
	 */
	private final ManagedObjectSourceInstance<?>[] managedObjectSourceInstances;

	/**
	 * {@link OfficeMetaData} for the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 */
	private final OfficeMetaData[] officeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param teams
	 *            Listing of {@link TeamManagement} instances.
	 * @param managedObjectSourceInstances
	 *            Listing of {@link ManagedObjectSourceInstance} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData} for the {@link Office} instances within
	 *            the {@link OfficeFloor}.
	 */
	public OfficeFloorMetaDataImpl(TeamManagement[] teams,
			ManagedObjectSourceInstance<?>[] managedObjectSourceInstances,
			OfficeMetaData[] officeMetaData) {
		this.teams = teams;
		this.managedObjectSourceInstances = managedObjectSourceInstances;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ================== OfficeFloorMetaData ==========================
	 */

	@Override
	public TeamManagement[] getTeams() {
		return this.teams;
	}

	@Override
	public OfficeMetaData[] getOfficeMetaData() {
		return this.officeMetaData;
	}

	@Override
	public ManagedObjectSourceInstance<?>[] getManagedObjectSourceInstances() {
		return this.managedObjectSourceInstances;
	}

}
