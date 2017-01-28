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
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory to create {@link RawAdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawAdministrationMetaDataFactory {

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
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param flowMetaDataFactory
	 *            {@link FlowMetaDataFactory}.
	 * @param escalationFlowFactory
	 *            {@link EscalationFlowFactory}.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link RawAdministrationMetaData} instances.
	 */
	RawAdministrationMetaData[] constructRawAdministrationMetaData(AdministrationConfiguration<?, ?, ?>[] configuration,
			AssetType assetType, String assetName, OfficeMetaData officeMetaData,
			FlowMetaDataFactory flowMetaDataFactory, EscalationFlowFactory escalationFlowFactory,
			Map<String, TeamManagement> officeTeams, Map<String, RawBoundManagedObjectMetaData> scopeMo,
			OfficeFloorIssues issues);

}