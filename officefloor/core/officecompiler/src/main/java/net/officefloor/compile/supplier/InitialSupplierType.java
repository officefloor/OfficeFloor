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

package net.officefloor.compile.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompileConfiguration;
import net.officefloor.compile.spi.supplier.source.SupplierCompileContext;

/**
 * <code>Type definition</code> of a Supplier that requires completing.
 * 
 * @author Daniel Sagenschneider
 */
public interface InitialSupplierType extends SupplierType {

	/**
	 * Obtains the {@link SupplierCompileCompletion} instances.
	 * 
	 * @return {@link SupplierCompileCompletion} instances.
	 */
	SupplierCompileCompletion[] getCompileCompletions();

	/**
	 * <p>
	 * Obtains the {@link SupplierCompileConfiguration} for the
	 * {@link SupplierCompileCompletion}.
	 * <p>
	 * While functionality is being sourced, the {@link SupplierCompileContext} may
	 * be utilised.
	 * 
	 * @return {@link SupplierCompileConfiguration}.
	 */
	SupplierCompileConfiguration getCompileConfiguration();

}
