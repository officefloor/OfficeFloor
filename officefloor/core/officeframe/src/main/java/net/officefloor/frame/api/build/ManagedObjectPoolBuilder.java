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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * Builder for the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolBuilder {

	/**
	 * <p>
	 * Adds a {@link ThreadCompletionListener}.
	 * <p>
	 * This allows the {@link ManagedObjectPool} to cache objects to
	 * {@link ThreadLocal} instances and be notified when the {@link Thread} is
	 * complete to clean up the {@link ThreadLocal} state.
	 * 
	 * @param threadCompletionListenerFactory
	 *            {@link ThreadCompletionListenerFactory}.
	 */
	void addThreadCompletionListener(ThreadCompletionListenerFactory threadCompletionListenerFactory);

}
