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

import net.officefloor.frame.api.manage.Office;

/**
 * Bindings to the {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeBindings {

	/**
	 * Builds the {@link ManagedObjectSourceNode} into the {@link Office}.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode}.
	 */
	void buildManagedObjectSourceIntoOffice(ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Builds the {@link BoundManagedObjectNode} into the {@link Office}.
	 * 
	 * @param managedObjectNode
	 *            {@link BoundManagedObjectNode}.
	 */
	void buildManagedObjectIntoOffice(BoundManagedObjectNode managedObjectNode);

	/**
	 * Builds the {@link InputManagedObjectNode} into the {@link Office}.
	 * 
	 * @param inputManagedObjectNode
	 *            {@link InputManagedObjectNode}.
	 */
	void buildInputManagedObjectIntoOffice(InputManagedObjectNode inputManagedObjectNode);

	/**
	 * Builds the {@link ManagedFunctionNode} into the {@link Office}.
	 * 
	 * @param managedFunctionNode
	 *            {@link ManagedFunctionNode}.
	 */
	void buildManagedFunctionIntoOffice(ManagedFunctionNode managedFunctionNode);

}
