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
package net.officefloor.compile.spi.pool.source;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Meta-data regarding the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSourceMetaData {

	/**
	 * Obtains the type of object expected to be pooled by this
	 * {@link ManagedObjectPool}.
	 * 
	 * @return Type of object expected to be pooled by this
	 *         {@link ManagedObjectPool}. This may be a super type of the actual
	 *         object.
	 */
	Class<?> getPooledObjectType();

	/**
	 * Obtains the {@link Thread} complete listener.
	 * 
	 * @return {@link Thread} complete listener. May be <code>null</code> if no
	 *         listener.
	 */
	Runnable getThreadCompleteListener();

}