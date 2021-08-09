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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Instance of a {@link ManagedObjectSource} and items to support it.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceInstance<F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, F> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectExecuteManagerFactory} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectExecuteManagerFactory} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectExecuteManagerFactory<F> getManagedObjectExecuteManagerFactory();

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if
	 *         {@link ManagedObjectSource} is not pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the {@link ManagedObjectServiceReady} instances.
	 * 
	 * @return {@link ManagedObjectServiceReady} instances.
	 */
	ManagedObjectServiceReady[] getServiceReadiness();

}
