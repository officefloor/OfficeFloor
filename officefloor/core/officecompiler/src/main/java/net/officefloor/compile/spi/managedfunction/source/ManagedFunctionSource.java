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
