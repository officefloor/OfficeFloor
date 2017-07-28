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
package net.officefloor.frame.impl.execute.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectPoolContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolContextImpl implements ManagedObjectPoolContext {

	/**
	 * {@link ThreadLocal} to indicate if the current {@link Thread} is managed.
	 */
	private static final ThreadLocal<Boolean> isCurrentThreadManaged = new ThreadLocal<>();

	/**
	 * Flags that the current {@link Thread} is managed.
	 */
	public static void flagCurrentThreadManaged() {
		isCurrentThreadManaged.set(Boolean.TRUE);
	}

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectPoolContextImpl(ManagedObjectSource<?, ?> managedObjectSource) {
		this.managedObjectSource = managedObjectSource;
	}

	/*
	 * ===================== ManagedObjectPoolContext ========================
	 */

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public boolean isCurrentThreadManaged() {
		return isCurrentThreadManaged.get() == Boolean.TRUE;
	}

}