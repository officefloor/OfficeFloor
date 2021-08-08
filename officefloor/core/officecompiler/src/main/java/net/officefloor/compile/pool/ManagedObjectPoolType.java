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

package net.officefloor.compile.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * <code>Type definition</code> of a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolType {

	/**
	 * Obtains the type of object being pooled.
	 * 
	 * @return Type of object being pooled.
	 */
	Class<?> getPooledObjectType();

	/**
	 * Obtains the {@link ManagedObjectPoolFactory} for the
	 * {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPoolFactory} for the
	 *         {@link ManagedObjectPool}.
	 */
	ManagedObjectPoolFactory getManagedObjectPoolFactory();

	/**
	 * Obtains the {@link ThreadCompletionListenerFactory} instances.
	 * 
	 * @return {@link ThreadCompletionListenerFactory} instances.
	 */
	ThreadCompletionListenerFactory[] getThreadCompletionListenerFactories();

}
