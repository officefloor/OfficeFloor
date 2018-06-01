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
package net.officefloor.frame.impl.construct.flow;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * {@link FlowMetaDataFactory} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class FlowMetaDataFactory {

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public FlowMetaDataFactory(OfficeMetaData officeMetaData) {
		this.officeMetaData = officeMetaData;
	}

	/**
	 * Creates the {@link FlowMetaData}.
	 * 
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param configurations
	 *            {@link FlowConfiguration} instances.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link FlowMetaData} or <code>null</code> if failed to create with
	 *         issues reported to the {@link OfficeFloorIssues}.
	 */
	public <F extends Enum<F>> FlowMetaData[] createFlowMetaData(FlowConfiguration<F>[] configurations,
			AssetType assetType, String assetName, OfficeFloorIssues issues) {

		// Obtain the function locator
		ManagedFunctionLocator functionLocator = officeMetaData.getManagedFunctionLocator();

		// Obtain the listing of flow meta-data
		FlowMetaData[] flowMetaDatas = new FlowMetaData[configurations.length];
		for (int i = 0; i < flowMetaDatas.length; i++) {
			FlowConfiguration<F> flowConfiguration = configurations[i];

			// Ensure have flow configuration
			if (flowConfiguration == null) {
				continue;
			}

			// Obtain the function reference
			ManagedFunctionReference functionReference = flowConfiguration.getInitialFunction();
			if (functionReference == null) {
				issues.addIssue(assetType, assetName, "No function referenced for flow index " + i);
				return null; // no reference task for flow
			}

			// Obtain the function meta-data
			ManagedFunctionMetaData<?, ?> functionMetaData = ConstructUtil.getFunctionMetaData(functionReference,
					functionLocator, issues, assetType, assetName, "flow index " + i);
			if (functionMetaData == null) {
				return null; // no initial function for flow
			}

			// Obtain whether to spawn thread state
			boolean isSpawnThreadState = flowConfiguration.isSpawnThreadState();

			// Create and add the flow meta-data
			flowMetaDatas[i] = ConstructUtil.newFlowMetaData(functionMetaData, isSpawnThreadState);
		}

		// Return the flow meta-data
		return flowMetaDatas;
	}

}