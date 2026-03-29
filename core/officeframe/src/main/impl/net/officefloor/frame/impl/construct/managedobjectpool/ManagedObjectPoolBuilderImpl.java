/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.managedobjectpool;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;

/**
 * Implements the {@link ManagedObjectPoolBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolBuilderImpl implements ManagedObjectPoolBuilder, ManagedObjectPoolConfiguration {

	/**
	 * {@link ManagedObjectPoolFactory}.
	 */
	private final ManagedObjectPoolFactory managedObjectPoolFactory;

	/**
	 * {@link ThreadCompletionListenerFactory} instances.
	 */
	private final List<ThreadCompletionListenerFactory> threadCompletionListenerFactories = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectPoolFactory
	 *            {@link ManagedObjectPoolFactory}.
	 */
	public ManagedObjectPoolBuilderImpl(ManagedObjectPoolFactory managedObjectPoolFactory) {
		this.managedObjectPoolFactory = managedObjectPoolFactory;
	}

	/*
	 * ======================= ManagedObjectPoolBuilder =======================
	 */

	@Override
	public void addThreadCompletionListener(ThreadCompletionListenerFactory threadCompletionListenerFactory) {
		this.threadCompletionListenerFactories.add(threadCompletionListenerFactory);
	}

	/*
	 * =================== ManagedObjectPoolConfiguration =====================
	 */

	@Override
	public ManagedObjectPoolFactory getManagedObjectPoolFactory() {
		return this.managedObjectPoolFactory;
	}

	@Override
	public ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories() {
		return this.threadCompletionListenerFactories.toArray(new ThreadCompletionListenerFactory[0]);
	}

}
