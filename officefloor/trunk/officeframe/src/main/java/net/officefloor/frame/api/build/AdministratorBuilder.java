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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Enables building an {@link Administrator}.
 * 
 * @author Daniel
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
	 * Name of the {@link Team} linked to the {@link Office} of this
	 * {@link Administrator} which is responsible for doing to the {@link Duty}
	 * instances of this {@link Administrator}.
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
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 * @param flowListingEnum
	 *            {@link Enum} {@link Class} listing {@link Flow} instances to
	 *            be instigated by the {@link Duty}.
	 * @return {@link DutyBuilder} for the specified {@link Duty}.
	 */
	<F extends Enum<F>> DutyBuilder<F> addDuty(A dutyKey,
			Class<F> flowListingEnum);

	/**
	 * Adds a {@link Duty} of the {@link Administrator}.
	 * 
	 * @param dutyKey
	 *            Key identifying the {@link Duty}.
	 * @return {@link DutyBuilder} for the specified {@link Duty}.
	 */
	DutyBuilder<Indexed> addDuty(A dutyKey);

}