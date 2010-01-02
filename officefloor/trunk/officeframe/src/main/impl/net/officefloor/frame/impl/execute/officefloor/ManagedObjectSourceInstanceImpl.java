/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSourceInstance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstanceImpl<F extends Enum<F>> implements
		ManagedObjectSourceInstance<F> {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, F> managedObjectSource;

	/**
	 * {@link ManagedObjectExecuteContext} for the {@link ManagedObjectSource}.
	 */
	private final ManagedObjectExecuteContext<F> managedObjectExecuteContext;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectExecuteContext
	 *            {@link ManagedObjectExecuteContext} for the
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 */
	public ManagedObjectSourceInstanceImpl(
			ManagedObjectSource<?, F> managedObjectSource,
			ManagedObjectExecuteContext<F> managedObjectExecuteContext,
			ManagedObjectPool managedObjectPool) {
		this.managedObjectSource = managedObjectSource;
		this.managedObjectExecuteContext = managedObjectExecuteContext;
		this.managedObjectPool = managedObjectPool;
	}

	/*
	 * ==================== ManagedObjectSourceInstance ==================
	 */

	@Override
	public ManagedObjectSource<?, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectExecuteContext<F> getManagedObjectExecuteContext() {
		return this.managedObjectExecuteContext;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

}