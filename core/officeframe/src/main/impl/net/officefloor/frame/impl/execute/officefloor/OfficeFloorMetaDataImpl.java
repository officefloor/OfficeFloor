/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
