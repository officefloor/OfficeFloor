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
package net.officefloor.frame.impl.execute.administrator;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorScope;

/**
 * {@link AdministratorIndex} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorIndexImpl implements AdministratorIndex {

	/**
	 * {@link AdministratorScope}.
	 */
	private final AdministratorScope administratorScope;

	/**
	 * Index of the {@link Administration} within the {@link AdministratorScope}.
	 */
	private final int indexOfAdministratorWithinScope;

	/**
	 * Initiate.
	 * 
	 * @param administratorScope
	 *            {@link AdministratorScope}.
	 * @param indexOfAdministratorWithinScope
	 *            Index of the {@link Administration} within the
	 *            {@link AdministratorScope}.
	 */
	public AdministratorIndexImpl(AdministratorScope administratorScope,
			int indexOfAdministratorWithinScope) {
		this.administratorScope = administratorScope;
		this.indexOfAdministratorWithinScope = indexOfAdministratorWithinScope;
	}

	/*
	 * ================== AdministratorIndex =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.AdministratorIndex#
	 * getAdministratorScope()
	 */
	@Override
	public AdministratorScope getAdministratorScope() {
		return this.administratorScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.AdministratorIndex#
	 * getIndexOfAdministratorWithinScope()
	 */
	@Override
	public int getIndexOfAdministratorWithinScope() {
		return this.indexOfAdministratorWithinScope;
	}

}
