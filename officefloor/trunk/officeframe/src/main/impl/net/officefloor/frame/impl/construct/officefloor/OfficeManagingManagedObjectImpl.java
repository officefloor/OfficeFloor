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
package net.officefloor.frame.impl.construct.officefloor;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobject.OfficeManagingManagedObject;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link OfficeManagingManagedObject} implementation.
 * 
 * @author Daniel
 */
public class OfficeManagingManagedObjectImpl implements
		OfficeManagingManagedObject {

	/**
	 * {@link ProcessState} bound name for the {@link ManagedObject} within the
	 * {@link Office}.
	 */
	private final String processBoundName;

	/**
	 * {@link RawManagedObjectMetaData} for the {@link ManagedObject}.
	 */
	private final RawManagedObjectMetaData<?, ?> rawManagedObjectMetaData;

	/**
	 * Initialise.
	 * 
	 * @param processBoundName
	 *            {@link ProcessState} bound name for the {@link ManagedObject}
	 *            within the {@link Office}.
	 * @param rawManagedObjectMetaData
	 *            {@link RawManagedObjectMetaData}.
	 */
	public OfficeManagingManagedObjectImpl(String processBoundName,
			RawManagedObjectMetaData<?, ?> rawManagedObjectMetaData) {
		this.processBoundName = processBoundName;
		this.rawManagedObjectMetaData = rawManagedObjectMetaData;
	}

	/*
	 * ================== OfficeManagingManagedObject ==========================
	 */

	@Override
	public String getProcessBoundName() {
		return this.processBoundName;
	}

	@Override
	public RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData() {
		return this.rawManagedObjectMetaData;
	}

}
