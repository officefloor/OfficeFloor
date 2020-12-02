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
