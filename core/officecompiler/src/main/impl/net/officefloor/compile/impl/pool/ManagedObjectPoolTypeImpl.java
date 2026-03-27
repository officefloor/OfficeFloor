/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
