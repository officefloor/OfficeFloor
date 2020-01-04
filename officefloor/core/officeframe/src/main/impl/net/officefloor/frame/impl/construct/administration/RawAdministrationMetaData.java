/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;

/**
 * Raw meta-data for the bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawAdministrationMetaData {

	/**
	 * {@link RawBoundManagedObjectMetaData} instances for the
	 * {@link Administration}.
	 */
	private final RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<?, ?, ?> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param rawBoundManagedObjectMetaData
	 *            {@link RawBoundManagedObjectMetaData} instances for the
	 *            {@link Administration}.
	 * @param administrationMetaData
	 *            {@link AdministrationMetaData}.
	 */
	public RawAdministrationMetaData(RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData,
			AdministrationMetaData<?, ?, ?> administrationMetaData) {
		this.rawBoundManagedObjectMetaData = rawBoundManagedObjectMetaData;
		this.administrationMetaData = administrationMetaData;
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} of the
	 * {@link ManagedObject} instances involved in the {@link Administration}.
	 * 
	 * @return {@link RawBoundManagedObjectMetaData} of the
	 *         {@link ManagedObject} instances involved in the
	 *         {@link Administration}.
	 */
	public RawBoundManagedObjectMetaData[] getRawBoundManagedObjectMetaData() {
		return this.rawBoundManagedObjectMetaData;
	}

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	public AdministrationMetaData<?, ?, ?> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}
