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

package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Factory for the construction of {@link RawTeamMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawTeamMetaDataFactory {

	/**
	 * Constructs the {@link RawTeamMetaData}.
	 * 
	 * @param configuration
	 *            {@link TeamConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link RawTeamMetaData} or <code>null</code> if fails to
	 *         construct.
	 */
	<TS extends TeamSource> RawTeamMetaData constructRawTeamMetaData(
			TeamConfiguration<TS> configuration, OfficeFloorIssues issues);

}
