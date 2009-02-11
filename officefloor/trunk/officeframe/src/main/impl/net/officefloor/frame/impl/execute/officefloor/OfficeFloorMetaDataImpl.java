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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link OfficeFloorMetaData} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorMetaDataImpl implements OfficeFloorMetaData {

	/**
	 * Listing of {@link Team} instances.
	 */
	private final Team[] teams;

	/**
	 * {@link OfficeMetaData} for the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 */
	private final OfficeMetaData[] officeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param teams
	 *            Listing of {@link Team} instances.
	 * @param officeMetaData
	 *            {@link OfficeMetaData} for the {@link Office} instances within
	 *            the {@link OfficeFloor}.
	 */
	public OfficeFloorMetaDataImpl(Team[] teams, OfficeMetaData[] officeMetaData) {
		this.teams = teams;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ================== OfficeFloorMetaData ==========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.OfficeFloorMetaData#getTeams()
	 */
	@Override
	public Team[] getTeams() {
		return this.teams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.OfficeFloorMetaData#
	 * getOfficeMetaData()
	 */
	@Override
	public OfficeMetaData[] getOfficeMetaData() {
		return this.officeMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.OfficeFloorMetaData#
	 * createOfficeFloor()
	 */
	@Override
	public OfficeFloor createOfficeFloor() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorMetaData.createOfficeFloor");
	}

}
