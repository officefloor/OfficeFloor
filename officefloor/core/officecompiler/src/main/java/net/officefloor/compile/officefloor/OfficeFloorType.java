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
package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of an {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorType {

	/**
	 * Obtains the {@link Property} instances to be configured for this
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link Property} instances to be configured for this
	 *         {@link OfficeFloor}.
	 */
	OfficeFloorPropertyType[] getOfficeFloorPropertyTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link ManagedObjectSource}
	 * instances that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorManagedObjectSourceType} instances.
	 */
	OfficeFloorManagedObjectSourceType[] getOfficeFloorManagedObjectSourceTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link TeamSource} instances
	 * that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorTeamSourceType} instances.
	 */
	OfficeFloorTeamSourceType[] getOfficeFloorTeamSourceTypes();

}