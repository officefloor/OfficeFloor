/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorManagedObjectSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceTypeImpl implements
		OfficeFloorManagedObjectSourceType {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String name;

	/**
	 * {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	private final OfficeFloorManagedObjectSourcePropertyType[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	public OfficeFloorManagedObjectSourceTypeImpl(String name,
			OfficeFloorManagedObjectSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ================= OfficeFloorManagedObjectSourceType =================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorManagedObjectSourcePropertyType[] getOfficeFloorManagedObjectSourcePropertyTypes() {
		return this.properties;
	}

}
