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

package net.officefloor.frame.api.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolContext {

	/**
	 * {@link ManagedObjectSource} to have its {@link ManagedObject} instances
	 * pooled.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, ?> getManagedObjectSource();

	/**
	 * Indicates if the current {@link Thread} is managed. A managed
	 * {@link Thread} will notify the {@link ThreadCompletionListener} instances
	 * of its completion.
	 * 
	 * @return <code>true</code> if the current {@link Thread} is managed.
	 */
	boolean isCurrentThreadManaged();

}
