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

package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Contract to provide control over asynchronous processing by the
 * {@link ManagedObject}.
 * <p>
 * Implemented by the {@link ManagedObjectSource} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousManagedObject extends ManagedObject {

	/**
	 * Provides the {@link AsynchronousContext} to the
	 * {@link AsynchronousManagedObject} to enable call back to notify state and
	 * completion of asynchronous processing.
	 * 
	 * @param context
	 *            {@link AsynchronousContext}.
	 */
	void setAsynchronousContext(AsynchronousContext context);

}
