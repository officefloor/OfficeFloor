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

import java.util.Map;

import net.officefloor.frame.spi.team.Team;

/**
 * Configuration for an {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorConfiguration {

	/**
	 * Obtains the configuration of the
	 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
	 * instances.
	 * 
	 * @return {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
	 *         configuration.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	ManagedObjectSourceConfiguration[] getManagedObjectSourceConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the registry of {@link Team} instances by their respective Id.
	 * 
	 * @return Registry of {@link Team} instances by their respective Id.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	Map<String, Team> getTeamRegistry() throws ConfigurationException;

	/**
	 * Obtains the configuration of the
	 * {@link net.officefloor.frame.api.manage.Office} instances on the
	 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 * 
	 * @return {@link net.officefloor.frame.api.manage.Office} configuration.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	OfficeConfiguration[] getOfficeConfiguration()
			throws ConfigurationException;

}
