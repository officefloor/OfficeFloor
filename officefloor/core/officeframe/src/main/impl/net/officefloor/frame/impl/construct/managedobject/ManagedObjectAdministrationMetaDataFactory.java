/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaData;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectAdministrationMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Factory for the createion of {@link ManagedObjectAdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectAdministrationMetaDataFactory {

	/**
	 * {@link RawAdministrationMetaDataFactory}.
	 */
	private final RawAdministrationMetaDataFactory rawAdminFactory;

	/**
	 * {@link ThreadState} scoped {@link RawBoundManagedObjectMetaData}
	 * instances.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> threadScopedManagedObjects;

	/**
	 * {@link ProcessState} scoped {@link RawBoundManagedObjectMetaData}
	 * instances.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> processScopedManagedObjects;

	/**
	 * Instantiate.
	 * 
	 * @param rawAdminFactory
	 *            {@link RawAdministrationMetaDataFactory}.
	 * @param threadScopedManagedObjects
	 *            {@link ThreadState} scoped
	 *            {@link RawBoundManagedObjectMetaData} instances.
	 * @param processScopedManagedObjects
	 *            {@link ProcessState} scoped
	 *            {@link RawBoundManagedObjectMetaData} instances.
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
	 * @param administrationConfiguration
	 *            {@link AdministrationConfiguration} instances.
	 * @param boundManagedObject
	 *            {@link RawBoundManagedObjectMetaData} being administered.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link ManagedObjectAdministrationMetaData} instances or
	 *         <code>null</code> if issues with issue reported to the
	 *         {@link OfficeFloorIssues}.
	 */
	public ManagedObjectAdministrationMetaData<?, ?, ?>[] createManagedObjectAdministrationMetaData(
			AdministrationConfiguration<?, ?, ?>[] administrationConfiguration,
			RawBoundManagedObjectMetaData boundManagedObject, OfficeFloorIssues issues) {

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
				administrationConfiguration, scopeMo, AssetType.MANAGED_OBJECT,
				boundManagedObject.getBoundManagedObjectName(), issues);
		if (rawAdministrations == null) {
			return null;
		}

		// Create the managed object administration
		ManagedObjectAdministrationMetaData<?, ?, ?>[] metaDatas = new ManagedObjectAdministrationMetaData[rawAdministrations.length];
		for (int a = 0; a < rawAdministrations.length; a++) {
			RawAdministrationMetaData rawAdministration = rawAdministrations[a];

			// Obtain the required managed objects
			RawBoundManagedObjectMetaData[] rawRequired = rawAdministration.getRawBoundManagedObjectMetaData();
			ManagedObjectIndex[] required = new ManagedObjectIndex[rawRequired.length];
			for (int m = 0; m < rawRequired.length; m++) {
				required[m] = rawRequired[m].getManagedObjectIndex();
			}

			// Create the managed object administration
			metaDatas[a] = new ManagedObjectAdministrationMetaDataImpl<>(required,
					rawAdministration.getAdministrationMetaData());
		}

		// Return the managed object administration
		return metaDatas;
	}

}