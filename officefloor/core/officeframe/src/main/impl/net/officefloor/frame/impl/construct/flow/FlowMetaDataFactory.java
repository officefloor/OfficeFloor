/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
