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
package net.officefloor.compile.administrator;

import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of an {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorType<I, A extends Enum<A>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the {@link DutyType} definitions for this
	 * {@link AdministratorType}.
	 * 
	 * @return {@link DutyType} definitions for this {@link AdministratorType}.
	 */
	DutyType<A, ?>[] getDutyTypes();

}