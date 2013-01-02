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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for a {@link Duty} to administer the {@link ManagedObject}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskDutyAssociation<A extends Enum<A>> {

	/**
	 * Obtains {@link AdministratorIndex} identifying the {@link Administrator}.
	 * 
	 * @return {@link AdministratorIndex} identifying the {@link Administrator}.
	 */
	AdministratorIndex getAdministratorIndex();

	/**
	 * Obtains the {@link DutyKey} identifying the {@link Duty} to be
	 * administered by the {@link Administrator}.
	 * 
	 * @return {@link DutyKey} identifying the {@link Duty} to be administered
	 *         by the {@link Administrator}.
	 */
	DutyKey<A> getDutyKey();

}