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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * Meta-data for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMetaData {

	/**
	 * Obtains the {@link OfficeMetaData} instances of the {@link Office} instances
	 * contained within the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeMetaData} instances.
	 */
	OfficeMetaData[] getOfficeMetaData();

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectSourceInstance} instances contained within
	 * the {@link OfficeFloor}.
	 * <p>
	 * They are ordered and grouped for starting. Order follows the first index,
	 * while each contained array is grouping to start in parallel.
	 * 
	 * @return {@link ManagedObjectSourceInstance} instances.
	 */
	ManagedObjectSourceInstance<?>[][] getManagedObjectSourceInstances();

	/**
	 * Obtains the {@link TeamManagement} over the {@link Team} instances of the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link TeamManagement} over the {@link Team} instances of the
	 *         {@link OfficeFloor}.
	 */
	TeamManagement[] getTeams();

	/**
	 * Obtains the maximum amount of time in milliseconds for {@link OfficeFloor} to
	 * start.
	 * 
	 * @return Maximum amount of time in milliseconds for {@link OfficeFloor} to
	 *         start.
	 */
	long getMaxStartupWaitTime();

}
