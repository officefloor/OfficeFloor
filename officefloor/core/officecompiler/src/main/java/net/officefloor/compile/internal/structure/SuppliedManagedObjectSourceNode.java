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

import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Node for the supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectSourceNode extends Node {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplied Managed Object Source";

	/**
	 * Initialises the {@link SuppliedManagedObjectSourceNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link SuppliedManagedObjectSourceType}. May be <code>null</code> if
	 *         issue in loading the {@link SuppliedManagedObjectSourceType}.
	 */
	SuppliedManagedObjectSourceType loadSuppliedManagedObjectSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SuppliedManagedObjectSource}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

}
