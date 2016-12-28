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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessMetaData {

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ProcessState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 *         instances bound to the {@link ProcessState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link AssetManager} for the main {@link ThreadState}.
	 * 
	 * @return {@link AssetManager} for the main {@link ThreadState}.
	 */
	AssetManager getMainThreadAssetManager();

	/**
	 * Obtains the {@link ThreadMetaData} of {@link ThreadState} instances
	 * spawned from the {@link ProcessState} of this {@link ProcessMetaData}.
	 * 
	 * @return {@link ThreadMetaData} of {@link ThreadState} instances spawned
	 *         from the {@link ProcessState} of this {@link ProcessMetaData}.
	 */
	ThreadMetaData getThreadMetaData();

}