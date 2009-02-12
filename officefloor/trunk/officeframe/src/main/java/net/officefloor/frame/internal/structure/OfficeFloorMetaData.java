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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorMetaData {

	/**
	 * Obtains the {@link Team} instances of the {@link OfficeFloor}.
	 * 
	 * @return {@link Team} instances of the {@link OfficeFloor}.
	 */
	Team[] getTeams();

	/**
	 * Obtains the {@link OfficeMetaData} instances of the {@link Office}
	 * instances contained within the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeMetaData} instances.
	 */
	OfficeMetaData[] getOfficeMetaData();

	/**
	 * Creates the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor createOfficeFloor();
}
