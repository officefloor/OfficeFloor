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
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;

/**
 * {@link ProcessMetaData} implementation.
 * 
 * @author Daniel
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
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances.
	 * @param administratorMetaData
	 *            {@link AdministratorMetaData} instances.
	 */
	public ProcessMetaDataImpl(
			ManagedObjectMetaData<?>[] managedObjectMetaData,
			AdministratorMetaData<?, ?>[] administratorMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.administratorMetaData = administratorMetaData;
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

}
