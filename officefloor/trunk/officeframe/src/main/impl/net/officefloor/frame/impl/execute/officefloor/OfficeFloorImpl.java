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
package net.officefloor.frame.impl.execute.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorImpl implements OfficeFloor {

	/**
	 * {@link OfficeFloorMetaData} for this {@link OfficeFloor}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorMetaData
	 *            {@link OfficeFloorMetaData}.
	 */
	public OfficeFloorImpl(OfficeFloorMetaData officeFloorMetaData) {
		this.officeFloorMetaData = officeFloorMetaData;
	}

	/*
	 * ====================== OfficeFloor ================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeFloor#openOfficeFloor()
	 */
	@Override
	public synchronized void openOfficeFloor() {

		// Ensure not already open
		if (this.offices != null) {
			throw new IllegalStateException("Office floor is already open");
		}

		// Start the teams working, necessary to create the offices
		for (Team team : this.officeFloorMetaData.getTeams()) {
			team.startWorking();
		}

		// Create the offices to open floor for work
		OfficeMetaData[] officeMetaDatas = this.officeFloorMetaData
				.getOfficeMetaData();
		this.offices = new HashMap<String, Office>(officeMetaDatas.length);
		for (OfficeMetaData officeMetaData : officeMetaDatas) {

			// Create the office
			String officeName = officeMetaData.getOfficeName();
			Office office = officeMetaData.createOffice();

			// Maintain reference to office for returning
			this.offices.put(officeName, office);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.OfficeFloor#closeOfficeFloor()
	 */
	@Override
	public synchronized void closeOfficeFloor() {

		// Ensure open to be closed
		if (this.offices == null) {
			// Not open, so do nothing
			return;
		}

		// Stop the teams working as closing
		for (Team team : this.officeFloorMetaData.getTeams()) {
			team.stopWorking();
		}

		// Flag that no longer open
		this.offices = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.manage.OfficeFloor#getOffice(java.lang.String)
	 */
	@Override
	public synchronized Office getOffice(String officeName) {

		// Ensure is open
		if (this.offices == null) {
			throw new IllegalStateException(
					"Must open the Office Floor before obtaining Offices");
		}

		// Return the office (if available)
		return this.offices.get(officeName);
	}

}
