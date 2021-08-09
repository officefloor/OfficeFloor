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

package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Supplies {@link ManagedObjectSource} instances.
 * <p>
 * This allows for plugging in object libraries.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierSource {

	/**
	 * <p>
	 * Obtains the {@link SupplierSourceSpecification} for this
	 * {@link SupplierSource}.
	 * <p>
	 * This enables the {@link SupplierSourceContext} to be populated with the
	 * necessary details as per this {@link SupplierSourceSpecification} in loading
	 * the {@link InitialSupplierType}.
	 * 
	 * @return {@link SupplierSourceSpecification}.
	 */
	SupplierSourceSpecification getSpecification();

	/**
	 * Supplies the necessary {@link ManagedObjectSource} instances.
	 * 
	 * @param context {@link SupplierSourceContext}.
	 * @throws Exception If fails to provide supply of {@link ManagedObjectSource}
	 *                   instances.
	 */
	void supply(SupplierSourceContext context) throws Exception;

	/**
	 * <p>
	 * Terminates the supply contract.
	 * <p>
	 * This should release all resources required by the supplier.
	 */
	void terminate();

}
