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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * {@link ManagedObjectGovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectGovernanceMetaDataImpl<I> implements
		ManagedObjectGovernanceMetaData<I>, ManagedObjectExtensionExtractor<I> {

	/**
	 * {@link Governance} index.
	 */
	private final int governanceIndex;

	/**
	 * {@link ExtensionInterfaceFactory}.
	 */
	private final ExtensionInterfaceFactory<I> extensionInterfaceFactory;

	/**
	 * Initiate.
	 * 
	 * @param governanceIndex
	 *            {@link Governance} index.
	 * @param extensionInterfaceFactory
	 *            {@link ExtensionInterfaceFactory}.
	 */
	public ManagedObjectGovernanceMetaDataImpl(int governanceIndex,
			ExtensionInterfaceFactory<I> extensionInterfaceFactory) {
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceFactory = extensionInterfaceFactory;
	}

	/*
	 * =================== ManagedObjectGovernanceMetaData ====================
	 */

	@Override
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	@Override
	public ManagedObjectExtensionExtractor<I> getExtensionInterfaceExtractor() {
		return this;
	}

	/*
	 * ========================= ExtensionInterfaceExtractor ===================
	 */

	@Override
	public I extractExtension(ManagedObject managedObject,
			ManagedObjectMetaData<?> managedObjectMetaData) {
		return this.extensionInterfaceFactory
				.createExtensionInterface(managedObject);
	}

}