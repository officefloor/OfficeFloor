/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
