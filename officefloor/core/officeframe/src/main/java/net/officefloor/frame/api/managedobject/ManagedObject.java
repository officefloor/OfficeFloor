/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Core interface of a Managed Object.
 * <p>
 * Additional managed functionality is available by implementing the following
 * interfaces:
 * <ol>
 * <li>{@link ContextAwareManagedObject}</li>
 * <li>{@link AsynchronousManagedObject}</li>
 * <li>{@link CoordinatingManagedObject}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObject {

	/**
	 * Obtains the object being managed.
	 * 
	 * @return Object being managed.
	 * @throws Throwable
	 *             Indicating failed to obtain the object for use.
	 */
	Object getObject() throws Throwable;

}