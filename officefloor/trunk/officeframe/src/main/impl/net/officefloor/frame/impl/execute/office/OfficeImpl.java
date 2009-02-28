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
package net.officefloor.frame.impl.execute.office;

import java.util.Map;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * {@link Office} implementation.
 * 
 * @author Daniel
 */
public class OfficeImpl implements Office {

	/**
	 * {@link OfficeMetaData} for this {@link Office}.
	 */
	private final OfficeMetaData metaData;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link OfficeMetaData}.
	 */
	public OfficeImpl(OfficeMetaData metaData) {
		this.metaData = metaData;
	}

	/*
	 * ====================== Office ==================================
	 */

	@Override
	public WorkManager getWorkManager(String workName)
			throws UnknownWorkException {

		// Obtain the work meta-data for the work
		for (WorkMetaData<?> workMetaData : this.metaData.getWorkMetaData()) {
			if (workMetaData.getWorkName().equals(workName)) {
				// Have the work meta-data, so return a work manager for it
				return new WorkManagerImpl(workMetaData, this.metaData);
			}
		}

		// Unknown work if at this point
		throw new UnknownWorkException(workName);
	}

	@Override
	public ManagedObject getManagedObject(String managedObjectId)
			throws Exception {

		// TODO determine how to expose managed object
		if (true)
			throw new UnsupportedOperationException(
					"TODO determine how to expose managed objects outside framework."
							+ " Need to consider exposing Managed Objects outside framework to provide flexibility.");

		Map<String, ManagedObjectSource<?, ?>> managedObjectSources = null;

		// References to load Managed Object
		final ManagedObject[] loadedManagedObject = new ManagedObject[1];
		final Throwable[] loadFailureCause = new Throwable[1];

		// Obtain the managed object source
		ManagedObjectSource<?, ?> source = managedObjectSources
				.get(managedObjectId);
		if (source == null) {
			throw new Exception("Unknown managed object '" + managedObjectId
					+ "'");
		}

		// Attempt to load the Managed Object
		source.sourceManagedObject(new ManagedObjectUser() {
			public void setManagedObject(ManagedObject managedObject) {
				synchronized (loadedManagedObject) {
					// Load the Managed Object
					loadedManagedObject[0] = managedObject;

					// Notify loaded
					loadedManagedObject.notify();
				}
			}

			public void setFailure(Throwable cause) {
				synchronized (loadedManagedObject) {
					// Flag the failure cause
					loadFailureCause[0] = cause;

					// Notify loaded
					loadedManagedObject.notify();
				}
			}
		});

		// Wait for Managed Object to be loaded
		for (;;) {
			// Return if loaded
			synchronized (loadedManagedObject) {

				// Check if obtained managed object
				if (loadedManagedObject[0] != null) {
					return loadedManagedObject[0];
				}

				// Propagate if failed to obtain
				if (loadFailureCause[0] != null) {
					throw new Error(loadFailureCause[0]);
				}

				// Wait to be loaded
				try {
					loadedManagedObject.wait();
				} catch (InterruptedException ex) {
					// Interrupted, therefore return null
					return null;
				}
			}
		}
	}

}
