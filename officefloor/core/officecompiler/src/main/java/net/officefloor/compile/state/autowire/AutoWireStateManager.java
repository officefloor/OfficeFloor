/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
