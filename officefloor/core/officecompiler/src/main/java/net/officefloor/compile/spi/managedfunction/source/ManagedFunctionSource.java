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

package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;

/**
 * Sources the {@link FunctionNamespaceType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionSource {

	/**
	 * <p>
	 * Obtains the {@link ManagedFunctionSourceSpecification} for this
	 * {@link ManagedFunctionSource}.
	 * <p>
	 * This enables the {@link ManagedFunctionSourceContext} to be populated with
	 * the necessary details as per this {@link ManagedFunctionSourceSpecification}
	 * in loading the {@link FunctionNamespaceType}.
	 * 
	 * @return {@link ManagedFunctionSourceSpecification}.
	 */
	ManagedFunctionSourceSpecification getSpecification();

	/**
	 * Sources the {@link FunctionNamespaceType} by populating it via the input
	 * {@link FunctionNamespaceBuilder}.
	 * 
	 * @param functionNamespaceTypeBuilder
	 *            {@link FunctionNamespaceBuilder} to be populated with the
	 *            <code>type definition</code> of the {@link ManagedFunctionSource}.
	 * @param context
	 *            {@link ManagedFunctionSourceContext} to source details to populate
	 *            the {@link FunctionNamespaceBuilder}.
	 * @throws Exception
	 *             If fails to populate the {@link FunctionNamespaceBuilder}.
	 */
	void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception;

}
