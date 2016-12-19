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

import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;

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
	 * {@link AdministratorMetaData} instances.
	 */
	private final AdministratorMetaData<?, ?>[] administratorMetaData;

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances.
	 * @param administratorMetaData
	 *            {@link AdministratorMetaData} instances.
	 * @param threadMetaData
	 *            {@link ThreadMetaData}.
	 */
	public ProcessMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData,
			AdministratorMetaData<?, ?>[] administratorMetaData, ThreadMetaData threadMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.administratorMetaData = administratorMetaData;
		this.threadMetaData = threadMetaData;
	}

	/*
	 * ============== ProcessMetaData =================================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
		return this.administratorMetaData;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

}