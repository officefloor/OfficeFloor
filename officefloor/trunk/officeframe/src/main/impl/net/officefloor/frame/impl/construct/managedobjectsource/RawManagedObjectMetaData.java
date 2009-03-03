/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Meta-data for a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface RawManagedObjectMetaData<D extends Enum<D>, H extends Enum<H>> {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getManagedObjectName();

	/**
	 * Sets up the {@link ManagedObjectSource} to be managed by the
	 * {@link Office} of the input {@link OfficeMetaData}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} of the {@link Office} managing this
	 *            {@link ManagedObjectSource}.
	 * @param indexOfManagedObjectInProcessState
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState} of the {@link Office}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void manageByOffice(OfficeMetaData officeMetaData,
			int indexOfManagedObjectInProcessState, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link ManagedObjectSourceConfiguration}.
	 * 
	 * @return {@link ManagedObjectSourceConfiguration}.
	 */
	ManagedObjectSourceConfiguration<H, ?> getManagedObjectSourceConfiguration();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<D, H> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceMetaData}.
	 */
	ManagedObjectSourceMetaData<D, H> getManagedObjectSourceMetaData();

	/**
	 * Obtains the default timeout for sourcing the {@link ManagedObject} and
	 * asynchronous operations.
	 * 
	 * @return Default timeout for sourcing the {@link ManagedObject} and
	 *         asynchronous operations.
	 */
	long getDefaultTimeout();

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if not pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the sourcing {@link AssetManager}.
	 * 
	 * @return Sourcing {@link AssetManager}.
	 */
	AssetManager getSourcingAssetManager();

	/**
	 * Obtains the operations {@link AssetManager}.
	 * 
	 * @return Operations {@link AssetManager}.
	 */
	AssetManager getOperationsAssetManager();

	/**
	 * Indicates if {@link AsynchronousManagedObject}.
	 * 
	 * @return <code>true</code> if {@link AsynchronousManagedObject}.
	 */
	boolean isAsynchronous();

	/**
	 * Indicates if {@link CoordinatingManagedObject}.
	 * 
	 * @return <code>true</code> if {@link CoordinatingManagedObject}.
	 */
	boolean isCoordinating();

	/**
	 * Obtains the name of the {@link Office} managing this
	 * {@link ManagedObject}.
	 * 
	 * @return Name of the {@link Office} managing this {@link ManagedObject}.
	 *         Should return <code>null</code> if not to be managed by an
	 *         {@link Office}.
	 */
	String getManagingOfficeName();

	/**
	 * <p>
	 * Obtains the name to bind the {@link ManagedObject} within the
	 * {@link ProcessState} of the managing {@link Office}.
	 * <p>
	 * Should {@link #getManagingOfficeName()} return <code>null</code> this
	 * method will be ignored.
	 * 
	 * @return Name to bind the {@link ManagedObject} within the
	 *         {@link ProcessState} of the managing {@link Office}.
	 */
	String getManagingOfficeProcessBoundManagedObjectName();

	/**
	 * Obtains the name of {@link Work} to recycle the {@link ManagedObject}.
	 * 
	 * @return Name of {@link Work} to recycle the {@link ManagedObject}.
	 */
	String getRecycleWorkName();

	/**
	 * Obtains the {@link Handler} instances for the {@link ManagedObjectSource}
	 * by the respective {@link Handler} key.
	 * 
	 * @return {@link Handler} instances for the {@link ManagedObjectSource}.
	 */
	Map<H, Handler<?>> getHandlers();

}