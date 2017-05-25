/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.frame.api.managedobject.pool;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Factory for the creation a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolFactory {

	/**
	 * Creates a {@link ManagedObjectPool} for the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} to have its objects pooled.
	 * @return {@link ManagedObjectPool}.
	 */
	ManagedObjectPool createManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource);

}