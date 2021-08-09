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

import net.officefloor.compile.spi.office.OfficeSupplierThreadLocal;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;

/**
 * Node for a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalNode
		extends LinkObjectNode, OfficeFloorSupplierThreadLocal, OfficeSupplierThreadLocal {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Supplier Thread Local";

	/**
	 * Initialises the {@link SupplierThreadLocalNode}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	void initialise(OptionalThreadLocalReceiver optionalThreadLocalReceiver);

	/**
	 * Obtains the {@link SupplierNode} containing this
	 * {@link SupplierThreadLocalNode}.
	 * 
	 * @return Parent {@link SupplierNode}.
	 */
	SupplierNode getSupplierNode();

	/**
	 * Builds the {@link SupplierThreadLocal}.
	 * 
	 * @param context {@link CompileContext}.
	 */
	void buildSupplierThreadLocal(CompileContext context);

}
