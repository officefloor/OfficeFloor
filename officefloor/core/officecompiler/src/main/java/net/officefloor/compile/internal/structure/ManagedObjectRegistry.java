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
