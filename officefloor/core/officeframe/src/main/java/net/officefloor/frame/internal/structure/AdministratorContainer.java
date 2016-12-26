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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Container for a {@link Duty} of the {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorContainer<A extends Enum<A>> {

	/**
	 * Obtains the {@link ThreadState} responsible for changes to this
	 * {@link AdministratorContainer}.
	 * 
	 * @return {@link ThreadState} responsible for changes to this
	 *         {@link AdministratorContainer}.
	 */
	ThreadState getResponsibleThreadState();

	/**
	 * Administers the {@link ManagedObject} instances for the
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @param functionDutyAssociation
	 *            {@link ManagedFunctionDutyAssociation}.
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer}.
	 * @param managedFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedFunctionContainer} to administer the
	 *         {@link ManagedObject} instances.
	 */
	ManagedFunctionContainer administerManagedObjects(ManagedFunctionDutyAssociation<A> functionDutyAssociation,
			ManagedFunctionContainer managedFunctionContainer, ManagedFunctionMetaData<?, ?> managedFunctionMetaData);

}