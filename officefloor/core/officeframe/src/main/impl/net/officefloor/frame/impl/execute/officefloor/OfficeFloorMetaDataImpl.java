/*-
 * #%L
 * OfficeFrame
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
	private final ManagedObjectSourceInstance<?>[][] managedObjectSourceInstances;

	/**
	 * {@link OfficeMetaData} for the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 */
	private final OfficeMetaData[] officeMetaData;

	/**
	 * Maximum time in milliseconds to wait for {@link OfficeFloor} to start.
	 */
	private final long maxStartupWaitTime;

	/**
	 * Initiate.
	 * 
	 * @param teams                        Listing of {@link TeamManagement}
	 *                                     instances.
	 * @param managedObjectSourceInstances Listing of
	 *                                     {@link ManagedObjectSourceInstance}
	 *                                     instances.
	 * @param officeMetaData               {@link OfficeMetaData} for the
	 *                                     {@link Office} instances within the
	 *                                     {@link OfficeFloor}.
	 * @param maxStartupWaitTime           Maximum time in milliseconds to wait for
	 *                                     {@link OfficeFloor} to start.
	 */
	public OfficeFloorMetaDataImpl(TeamManagement[] teams,
			ManagedObjectSourceInstance<?>[][] managedObjectSourceInstances, OfficeMetaData[] officeMetaData,
			long maxStartupWaitTime) {
		this.teams = teams;
		this.managedObjectSourceInstances = managedObjectSourceInstances;
		this.officeMetaData = officeMetaData;
		this.maxStartupWaitTime = maxStartupWaitTime;
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
	public ManagedObjectSourceInstance<?>[][] getManagedObjectSourceInstances() {
		return this.managedObjectSourceInstances;
	}

	@Override
	public long getMaxStartupWaitTime() {
		return this.maxStartupWaitTime;
	}

}
