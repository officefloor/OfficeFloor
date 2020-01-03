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
