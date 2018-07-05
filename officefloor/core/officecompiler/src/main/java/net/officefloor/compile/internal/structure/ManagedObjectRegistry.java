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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Registry of the {@link ManagedObjectNode} within a particular context (for
 * example {@link SectionNode}).
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectRegistry {

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectNode} from the registry.
	 * <p>
	 * The returned {@link ManagedObjectNode} may or may not be initialised.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @return {@link ManagedObjectNode} from the registry.
	 */
	ManagedObjectNode getManagedObjectNode(String managedObjectName);

	/**
	 * <p>
	 * Adds an initialised {@link ManagedObjectNode} to the registry.
	 * <p>
	 * Should an {@link ManagedObjectNode} already be added by the name, then an
	 * issue is reported to the {@link CompilerIssue}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectNode}.
	 * @return Initialised {@link ManagedObjectNode} by the name.
	 */
	ManagedObjectNode addManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode);

}