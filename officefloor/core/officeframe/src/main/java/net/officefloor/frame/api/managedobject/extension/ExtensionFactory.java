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