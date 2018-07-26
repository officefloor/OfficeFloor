/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.executive.Executive;
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
	 * {@link Executive} to provide the process identifiers.
	 */
	private final Executive executive;

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * Initiate.
	 * 
	 * @param executive             {@link Executive}.
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} instances.
	 * @param threadMetaData        {@link ThreadMetaData}.
	 */
	public ProcessMetaDataImpl(Executive executive, ManagedObjectMetaData<?>[] managedObjectMetaData,
			ThreadMetaData threadMetaData) {
		this.executive = executive;
		this.managedObjectMetaData = managedObjectMetaData;
		this.threadMetaData = threadMetaData;
	}

	/*
	 * ============== ProcessMetaData =================================
	 */

	@Override
	public Object createProcessIdentifier() {
		return this.executive.createProcessIdentifier();
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

}