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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface RawOfficeFloorMetaData {

	/**
	 * Obtains the {@link RawTeamMetaData} for the {@link Team} name.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link RawTeamMetaData} or <code>null</code> if not exist for
	 *         name.
	 */
	RawTeamMetaData getRawTeamMetaData(String teamName);

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the
	 * {@link ManagedObjectSource} name.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaData} or <code>null</code> if not
	 *         exist for name.
	 */
	RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData(
			String managedObjectSourceName);

	/**
	 * Obtains the {@link EscalationFlow} for the {@link OfficeFloor}.
	 * 
	 * @return {@link EscalationFlow}.
	 */
	EscalationFlow getOfficeFloorEscalation();

	/**
	 * Obtains the {@link OfficeFloorMetaData}.
	 * 
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link OfficeFloorMetaData}.
	 */
	OfficeFloorMetaData getOfficeFloorMetaData();

}