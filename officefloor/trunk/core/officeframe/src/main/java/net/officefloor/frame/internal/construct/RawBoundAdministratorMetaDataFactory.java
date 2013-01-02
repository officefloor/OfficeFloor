/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Factory to create {@link RawBoundAdministratorMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawBoundAdministratorMetaDataFactory {

	/**
	 * Creates the {@link RawBoundAdministratorMetaData} instances.
	 * 
	 * @param configuration
	 *            {@link AdministratorSourceConfiguration} instances.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param administratorScope
	 *            {@link AdministratorScope} that the {@link Administrator}
	 *            instances are being bound.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administrator}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administrator}
	 *            instances.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @return Constructed {@link RawBoundAdministratorMetaData} instances.
	 */
	RawBoundAdministratorMetaData<?, ?>[] constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<?, ?>[] configuration,
			SourceContext sourceContext, OfficeFloorIssues issues,
			AdministratorScope administratorScope, AssetType assetType,
			String assetName, Map<String, TeamManagement> officeTeams,
			Team continueTeam,
			Map<String, RawBoundManagedObjectMetaData> scopeMo);

}