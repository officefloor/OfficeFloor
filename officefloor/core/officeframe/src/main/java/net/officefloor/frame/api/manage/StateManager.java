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

package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Manages state (multiple {@link ManagedObject} instances used externally).
 * <p>
 * {@link ManagedObject} instances are kept alive until the {@link StateManager}
 * is closed.
 * 
 * @author Daniel Sagenschneider
 */
public interface StateManager extends AutoCloseable {

	/**
	 * Loads the object from the {@link ManagedObject} asynchronously.
	 * 
	 * @param boundObjectName Bound name of the {@link ManagedObject}.
	 * @param user            {@link ObjectUser} to receive the loaded object (or
	 *                        possible failure).
	 * @throws UnknownObjectException If unknown bound object name.
	 */
	<O> void load(String boundObjectName, ObjectUser<O> user) throws UnknownObjectException;

	/**
	 * Obtains the object for the {@link ManagedObject} synchronously.
	 * 
	 * @param boundObjectName       Bound name of the {@link ManagedObject}.
	 * @param timeoutInMilliseconds Time out in milliseconds to wait for the
	 *                              {@link ManagedObject} creation.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	<O> O getObject(String boundObjectName, long timeoutInMilliseconds) throws UnknownObjectException, Throwable;

}
