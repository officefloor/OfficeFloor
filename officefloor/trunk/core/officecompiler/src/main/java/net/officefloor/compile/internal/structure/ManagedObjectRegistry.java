/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Registry of the {@link ManagedObjectNode} within a particular context (for
 * example {@link SectionNode}).
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectRegistry {

	/**
	 * Obtains the {@link ManagedObjectNode} from the registry.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode} to obtain.
	 * @return {@link ManagedObjectNode} or <code>null</code> if no
	 *         {@link ManagedObjectNode} registered.
	 */
	ManagedObjectNode getManagedObjectNode(String managedObjectName);

	/**
	 * Creates a new {@link ManagedObjectNode} and registers it.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @return {@link ManagedObjectNode}.
	 */
	ManagedObjectNode createManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope);

}