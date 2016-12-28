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
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ProcessMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMetaDataImpl implements ProcessMetaData {

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * {@link AssetManager} for the main {@link ThreadState}.
	 */
	private final AssetManager mainThreadAssetManager;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances.
	 * @param threadMetaData
	 *            {@link ThreadMetaData}.
	 * @param mainThreadAssetManager
	 *            {@link AssetManager} for the main {@link ThreadState}.
	 */
	public ProcessMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData, ThreadMetaData threadMetaData,
			AssetManager mainThreadAssetManager) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.threadMetaData = threadMetaData;
		this.mainThreadAssetManager = mainThreadAssetManager;
	}

	/*
	 * ============== ProcessMetaData =================================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public AssetManager getMainThreadAssetManager() {
		return this.mainThreadAssetManager;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

}