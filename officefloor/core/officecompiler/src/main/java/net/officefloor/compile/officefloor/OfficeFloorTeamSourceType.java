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
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of a {@link TeamSource} available to be
 * configured in the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamSourceType {

	/**
	 * Obtains the name of the {@link TeamSource} within the {@link OfficeFloor}
	 * that may be configured.
	 * 
	 * @return Name of the {@link TeamSource} within the {@link OfficeFloor}
	 *         that may be configured.
	 */
	String getOfficeFloorTeamSourceName();

	/**
	 * Obtains the {@link OfficeFloorTeamSourcePropertyType} instances identify
	 * the {@link Property} instances that may be configured for this
	 * {@link TeamSource}.
	 * 
	 * @return {@link OfficeFloorTeamSourcePropertyType} instances.
	 */
	OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes();

}