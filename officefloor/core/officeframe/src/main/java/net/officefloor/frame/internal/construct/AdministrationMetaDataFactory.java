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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory to create {@link RawAdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationMetaDataFactory {

	/**
	 * Creates the {@link RawAdministrationMetaData} instances.
	 * 
	 * @param configuration
	 *            {@link AdministrationConfiguration} instances.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administration}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administration}
	 *            instances.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator} for the {@link Office}.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @param governanceMetaDatas
	 *            {@link GovernanceMetaData} instances for the {@link Office}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link AdministrationMetaData} instances.
	 */
	AdministrationMetaData<?, ?, ?>[] constructAdministrationMetaData(
			AdministrationConfiguration<?, ?, ?>[] configuration, AssetType assetType, String assetName,
			Map<String, TeamManagement> officeTeams, ManagedFunctionLocator functionLocator,
			Map<String, RawBoundManagedObjectMetaData> scopeMo, GovernanceMetaData<?, ?>[] governanceMetaDatas,
			FunctionLoop functionLoop, OfficeFloorIssues issues);

}