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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;

/**
 * Meta-data regarding an extension interface implemented by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionMetaData<E> {

	/**
	 * Obtains the type of extension.
	 * 
	 * @return {@link Class} representing the type of extension.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link ExtensionFactory} to create the extension for the
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ExtensionFactory} to create the extension for the
	 *         {@link ManagedObject}.
	 */
	ExtensionFactory<E> getExtensionFactory();

}
