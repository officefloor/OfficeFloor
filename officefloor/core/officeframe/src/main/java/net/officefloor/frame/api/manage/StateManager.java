/*-
 * #%L
 * OfficeFrame
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
