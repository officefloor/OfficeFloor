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

import java.util.Properties;

import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of the {@link AdministratorSource}.
 * 
 * @author Daniel
 */
public interface AdministratorSourceConfiguration {

	/**
	 * Obtains the name of the {@link AdministratorSource}.
	 * 
	 * @return Name of the {@link AdministratorSource}.
	 */
	String getAdministratorName();

	/**
	 * Obtains the {@link Class} of the {@link AdministratorSource}.
	 * 
	 * @return {@link Class} of the {@link AdministratorSource}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	<AS extends AdministratorSource<?, ?>> Class<AS> getAdministratorSourceClass()
			throws ConfigurationException;

	/**
	 * Obtains the properties to initialise the {@link AdministratorSource}.
	 * 
	 * @return Properties to initialise the {@link AdministratorSource}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link OfficeScope} for this {@link Administrator}.
	 * 
	 * @return {@link OfficeScope} for this {@link Administrator}.
	 */
	OfficeScope getAdministratorScope();

	/**
	 * Obtains the name of the {@link Team} linked to the {@link Office}
	 * responsible for completing this {@link Duty} instances of this
	 * {@link Administrator}.
	 * 
	 * @return Id of the {@link Team}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	String getTeamName() throws ConfigurationException;

	/**
	 * Obtains the listing of {@link DutyConfiguration} for the {@link Duty}
	 * instances of this {@link Administrator}.
	 * 
	 * @return Listing of {@link DutyConfiguration} for the {@link Duty}
	 *         instances of this {@link Administrator}.
	 */
	DutyConfiguration<?>[] getDutyConfiguration();

}
