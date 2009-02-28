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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration for an {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorConfiguration {

	/**
	 * Obtains the name of the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	String getOfficeFloorName();

	/**
	 * Obtains the configuration of the {@link ManagedObjectSource} instances.
	 * 
	 * @return {@link ManagedObjectSource} configuration.
	 */
	ManagedObjectSourceConfiguration<?, ?>[] getManagedObjectSourceConfiguration();

	/**
	 * Obtains the configuration of the {@link Team} instances on the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link TeamConfiguration} instances.
	 */
	TeamConfiguration<?>[] getTeamConfiguration();

	/**
	 * Obtains the configuration of the {@link Office} instances on the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeConfiguration} instances.
	 */
	OfficeConfiguration[] getOfficeConfiguration();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link OfficeFloor}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link OfficeFloor}. May be
	 *         <code>null</code>.
	 */
	EscalationProcedure getEscalationProcedure();

}
