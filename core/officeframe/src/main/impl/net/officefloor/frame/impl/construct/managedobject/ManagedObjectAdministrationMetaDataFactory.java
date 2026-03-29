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

package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaData;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectAdministrationMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Factory for the creation of {@link ManagedObjectAdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectAdministrationMetaDataFactory {

	/**
	 * {@link RawAdministrationMetaDataFactory}.
	 */
	private final RawAdministrationMetaDataFactory rawAdminFactory;

	/**
	 * {@link ThreadState} scoped {@link RawBoundManagedObjectMetaData} instances.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> threadScopedManagedObjects;

	/**
	 * {@link ProcessState} scoped {@link RawBoundManagedObjectMetaData} instances.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> processScopedManagedObjects;

	/**
	 * Instantiate.
	 * 
	 * @param rawAdminFactory             {@link RawAdministrationMetaDataFactory}.
	 * @param threadScopedManagedObjects  {@link ThreadState} scoped
	 *                                    {@link RawBoundManagedObjectMetaData}
	 *                                    instances.
	 * @param processScopedManagedObjects {@link ProcessState} scoped
	 *                                    {@link RawBoundManagedObjectMetaData}
	 *                                    instances.
	 */
	public ManagedObjectAdministrationMetaDataFactory(RawAdministrationMetaDataFactory rawAdminFactory,
			Map<String, RawBoundManagedObjectMetaData> threadScopedManagedObjects,
			Map<String, RawBoundManagedObjectMetaData> processScopedManagedObjects) {
		this.rawAdminFactory = rawAdminFactory;
		this.threadScopedManagedObjects = threadScopedManagedObjects;
		this.processScopedManagedObjects = processScopedManagedObjects;
	}

	/**
	 * Constructs the {@link ManagedObjectAdministrationMetaData} instances.
	 * 
	 * @param administeredObjectName         Name of {@link Asset} adding
	 *                                       {@link Administration}.
	 * @param administrationConfiguration    {@link AdministrationConfiguration}
	 *                                       instances.
	 * @param boundManagedObject             {@link RawBoundManagedObjectMetaData}
	 *                                       being administered.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return {@link ManagedObjectAdministrationMetaData} instances or
	 *         <code>null</code> if issues with issue reported to the
	 *         {@link OfficeFloorIssues}.
	 */
	public ManagedObjectAdministrationMetaData<?, ?, ?>[] createManagedObjectAdministrationMetaData(
			String administeredObjectName, AdministrationConfiguration<?, ?, ?>[] administrationConfiguration,
			RawBoundManagedObjectMetaData boundManagedObject, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the appropriate scope managed objects
		ManagedObjectScope scope = boundManagedObject.getManagedObjectIndex().getManagedObjectScope();
		Map<String, RawBoundManagedObjectMetaData> scopeMo;
		switch (scope) {
		case FUNCTION:
			scopeMo = this.threadScopedManagedObjects;
			break;
		case THREAD:
			scopeMo = this.processScopedManagedObjects;
			break;
		case PROCESS:
			scopeMo = new HashMap<>();
			break;
		default:
			throw new IllegalStateException("Unknown scope " + scope);
		}

		// Construct the raw administration
		RawAdministrationMetaData[] rawAdministrations = rawAdminFactory.constructRawAdministrationMetaData(
				administeredObjectName, "load", administrationConfiguration, scopeMo, AssetType.MANAGED_OBJECT,
				boundManagedObject.getBoundManagedObjectName(), assetManagerRegistry, defaultAsynchronousFlowTimeout,
				issues);
		if (rawAdministrations == null) {
			return null;
		}

		// Create the managed object administration
		ManagedObjectAdministrationMetaData<?, ?, ?>[] metaDatas = new ManagedObjectAdministrationMetaData[rawAdministrations.length];
		for (int a = 0; a < rawAdministrations.length; a++) {
			RawAdministrationMetaData rawAdministration = rawAdministrations[a];

			// Obtain the administration
			AdministrationMetaData<?, ?, ?> adminMetaData = rawAdministration.getAdministrationMetaData();

			// Obtain the required managed objects
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects = new HashMap<>();
			for (RawBoundManagedObjectMetaData extension : rawAdministration.getRawBoundManagedObjectMetaData()) {
				RawBoundManagedObjectMetaData.loadRequiredManagedObjects(extension, requiredManagedObjects);
			}

			// Create the sorted required managed objects
			ManagedObjectIndex[] required = RawBoundManagedObjectMetaData.createSortedRequiredManagedObjects(
					requiredManagedObjects, AssetType.ADMINISTRATOR, adminMetaData.getAdministrationName(), issues);
			if (required == null) {
				return null;
			}

			// Create the logger
			String loggerName = administeredObjectName + ".pre." + adminMetaData.getAdministrationName();
			Logger logger = OfficeFrame.getLogger(loggerName);

			// Create the managed object administration
			metaDatas[a] = new ManagedObjectAdministrationMetaDataImpl<>(required, adminMetaData, logger);
		}

		// Return the managed object administration
		return metaDatas;
	}

}
