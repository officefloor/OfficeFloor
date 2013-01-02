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

/**
 * Index of the {@link Administrator}, providing both the scope it lives within
 * and the index of it within that scope.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorIndex {

	/**
	 * Obtains the {@link AdministratorScope} that the {@link Administrator}
	 * resides within.
	 * 
	 * @return {@link AdministratorScope} that the {@link Administrator} resides
	 *         within.
	 */
	AdministratorScope getAdministratorScope();

	/**
	 * Obtains the index of the {@link Administrator} within the
	 * {@link AdministratorScope}.
	 * 
	 * @return Index of the {@link Administrator} within the
	 *         {@link AdministratorScope}.
	 */
	int getIndexOfAdministratorWithinScope();

}