/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.internal.configuration;

import java.util.Properties;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of the {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorSourceConfiguration<A extends Enum<A>, AS extends AdministratorSource<?, A>> {

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
	 */
	Class<AS> getAdministratorSourceClass();

	/**
	 * Obtains the properties to initialise the {@link AdministratorSource}.
	 * 
	 * @return Properties to initialise the {@link AdministratorSource}.
	 */
	Properties getProperties();

	/**
	 * Obtains the name of the {@link Team} within the {@link Office}
	 * responsible for completing the {@link Duty} instances of this
	 * {@link Administrator}.
	 * 
	 * @return {@link Office} name of the {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the names of the {@link ManagedObject} instances to be
	 * administered.
	 * 
	 * @return Names of the {@link ManagedObject} instances to be administered.
	 */
	String[] getAdministeredManagedObjectNames();

	/**
	 * Obtains the listing of {@link DutyConfiguration} for the {@link Duty}
	 * instances of this {@link Administrator}.
	 * 
	 * @return Listing of {@link DutyConfiguration} for the {@link Duty}
	 *         instances of this {@link Administrator}.
	 */
	DutyConfiguration<A>[] getDutyConfiguration();

}