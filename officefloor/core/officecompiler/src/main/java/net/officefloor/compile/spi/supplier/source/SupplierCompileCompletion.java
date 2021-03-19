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

package net.officefloor.compile.spi.supplier.source;

/**
 * Invoke on completion of compiling.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface SupplierCompileCompletion {

	/**
	 * Invoked on completion of compiling.
	 * 
	 * @param context {@link SupplierCompletionContext}.
	 * @throws Exception Failure in handling completion.
	 */
	void complete(SupplierCompletionContext context) throws Exception;

}
