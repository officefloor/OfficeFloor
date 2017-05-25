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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * Configuration for the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolConfiguration {

	/**
	 * Obtains the {@link ManagedObjectPoolFactory}.
	 * 
	 * @return {@link ManagedObjectPoolFactory}.
	 */
	ManagedObjectPoolFactory getManagedObjectPoolFactory();

	/**
	 * Obtains the {@link ThreadCompletionListenerFactory} instances.
	 * 
	 * @return {@link ThreadCompletionListenerFactory} instances.
	 */
	ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories();

}