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

package net.officefloor.frame.api.managedobject.extension;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Creates a specific extension for the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ExtensionFactory<E> {

	/**
	 * Creates the specific extension for the {@link ManagedObject}.
	 *
	 * @param managedObject
	 *            {@link ManagedObject} that is have the extension created for
	 *            it.
	 * @return Extension for the {@link ManagedObject}.
	 * @throws Throwable
	 *             If fails to create extension.
	 */
	E createExtension(ManagedObject managedObject) throws Throwable;

}
