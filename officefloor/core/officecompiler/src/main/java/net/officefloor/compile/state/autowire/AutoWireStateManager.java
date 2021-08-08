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

package net.officefloor.compile.state.autowire;

import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link StateManager} that enables obtaining {@link ManagedObject} objects by
 * their qualifications and types.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManager extends AutoCloseable {

	/**
	 * Indicates if the object by auto-wiring is available.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @return <code>true</code> if the object is available.
	 */
	boolean isObjectAvailable(String qualifier, Class<?> objectType);

	/**
	 * Loads the object from the {@link ManagedObject} asynchronously.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @param user       {@link ObjectUser} to receive the loaded object (or
	 *                   possible failure).
	 * @throws UnknownObjectException If unknown bound object name.
	 */
	<O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user) throws UnknownObjectException;

	/**
	 * Obtains the object for the {@link ManagedObject} synchronously.
	 * 
	 * @param qualifier             Qualifier. May be <code>null</code>.
	 * @param objectType            Required object type.
	 * @param timeoutInMilliseconds Time out in milliseconds to wait for the
	 *                              {@link ManagedObject} creation.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	<O> O getObject(String qualifier, Class<? extends O> objectType, long timeoutInMilliseconds)
			throws UnknownObjectException, Throwable;

}
