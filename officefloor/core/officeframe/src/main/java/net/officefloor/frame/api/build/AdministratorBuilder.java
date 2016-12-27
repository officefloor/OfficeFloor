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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Enables building an {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorBuilder<A extends Enum<A>> {

	/**
	 * Specifies a property for the {@link AdministratorSource}.
	 * 
	 * @param name
	 *            Name of property.
	 * @param value
	 *            Value of property.
	 */
	void addProperty(String name, String value);

	/**
	 * <p>
	 * Name of the {@link Team} linked to the {@link Office} of this
	 * {@link Administrator} which is responsible for doing to the {@link Duty}
	 * instances of this {@link Administrator}.
	 * <p>
	 * If not specified, any {@link Team} will be used.
	 * 
	 * @param officeTeamName
	 *            Name of {@link Team} within the {@link Office} for this
	 *            {@link Administrator}.
	 */
	void setTeam(String officeTeamName);

	/**
	 * Flags for the {@link Administrator} to administer the referenced
	 * {@link ManagedObject}. This may be called more than once to register more
	 * than one {@link ManagedObject} to be administered by this
	 * {@link Administrator}.
	 * 
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the scope this
	 *            {@link Administrator} is being added.
	 */
	void administerManagedObject(String scopeManagedObjectName);

	/**
	 * Adds a {@link Duty} of the {@link Administrator}.
	 * 
	 * @param dutyName
	 *            Name identifying the {@link Duty}.
	 * @return {@link DutyBuilder} for the specified {@link Duty}.
	 */
	DutyBuilder addDuty(String dutyName);

}