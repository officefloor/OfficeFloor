/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMetaData {

	/**
	 * Obtains the {@link OfficeMetaData} instances of the {@link Office}
	 * instances contained within the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeMetaData} instances.
	 */
	OfficeMetaData[] getOfficeMetaData();

	/**
	 * Obtains the {@link ManagedObjectSourceInstance} instances contained
	 * within the {@link OfficeFloor}.
	 * 
	 * @return {@link ManagedObjectSourceInstance} instances.
	 */
	ManagedObjectSourceInstance<?>[] getManagedObjectSourceInstances();

	/**
	 * Obtains the {@link Team} instances of the {@link OfficeFloor}.
	 * 
	 * @return {@link Team} instances of the {@link OfficeFloor}.
	 */
	Team[] getTeams();

}
