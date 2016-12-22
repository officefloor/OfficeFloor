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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * {@link Office} implementation.
 * 
 * @author Daniel Sagenschneider
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
	public String[] getWorkNames() {

		// Obtain the work names
		WorkMetaData<?>[] workMetaData = this.metaData.getWorkMetaData();
		String[] workNames = new String[workMetaData.length];
		for (int i = 0; i < workNames.length; i++) {
			workNames[i] = workMetaData[i].getWorkName();
		}

		// Return the work names
		return workNames;
	}

	@Override
	public WorkManager getWorkManager(String workName) throws UnknownWorkException {

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

}