/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.pool;

import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * {@link ManagedObjectPoolType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolTypeImpl implements ManagedObjectPoolType {

	/**
	 * Pooled object type.
	 */
	private final Class<?> pooledObjectType;

	/**
	 * {@link ManagedObjectPoolFactory}.
	 */
	private final ManagedObjectPoolFactory managedObjectPoolFactory;

	/**
	 * {@link ThreadCompletionListenerFactory} instances.
	 */
	private final ThreadCompletionListenerFactory[] threadCompletionListenerFactories;

	/**
	 * Instantiate.
	 * 
	 * @param pooledObjectType
	 *            Pooled object type.
	 * @param managedObjectPoolFactory
	 *            {@link ManagedObjectPoolFactory}.
	 * @param threadCompletionListenerFactories
	 *            {@link ThreadCompletionListenerFactory} instances.
	 */
	public ManagedObjectPoolTypeImpl(Class<?> pooledObjectType, ManagedObjectPoolFactory managedObjectPoolFactory,
			ThreadCompletionListenerFactory[] threadCompletionListenerFactories) {
		this.pooledObjectType = pooledObjectType;
		this.managedObjectPoolFactory = managedObjectPoolFactory;
		this.threadCompletionListenerFactories = threadCompletionListenerFactories;
	}

	/*
	 * ==================== ManagedObjectPoolType =======================
	 */

	@Override
	public Class<?> getPooledObjectType() {
		return this.pooledObjectType;
	}

	@Override
	public ManagedObjectPoolFactory getManagedObjectPoolFactory() {
		return this.managedObjectPoolFactory;
	}

	@Override
	public ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories() {
		return this.threadCompletionListenerFactories;
	}

}