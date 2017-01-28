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

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * Factory to create the {@link FlowMetaData}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowMetaDataFactory {

	/**
	 * Creates the {@link FlowMetaData}.
	 * 
	 * @param configurations
	 *            {@link FlowConfiguration} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link FlowMetaData} or <code>null</code> if failed to create
	 *         with issues reported to the {@link OfficeFloorIssues}.
	 */
	<F extends Enum<F>> FlowMetaData[] createFlowMetaData(FlowConfiguration<F>[] configurations,
			OfficeMetaData officeMetaData, AssetType assetType, String assetName, OfficeFloorIssues issues);

}