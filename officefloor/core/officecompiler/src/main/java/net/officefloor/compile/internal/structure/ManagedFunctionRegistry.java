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
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Registry of the {@link ManagedFunctionNode} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionRegistry {

	/**
	 * <p>
	 * Adds an initialised {@link ManagedFunctionNode} to the registry.
	 * <p>
	 * Should an {@link ManagedFunctionNode} already be added by the name, then an
	 * issue is reported to the {@link CompilerIssue}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunctionNode}.
	 * @param functionTypeName
	 *            Type name of the {@link ManagedFunction} within the
	 *            {@link ManagedFunctionSource}.
	 * @param functionNamespaceNode
	 *            Parent {@link FunctionNamespaceNode}.
	 * @return Initialised {@link ManagedFunctionNode} by the name.
	 */
	ManagedFunctionNode addManagedFunctionNode(String functionName, String functionTypeName,
			FunctionNamespaceNode functionNamespaceNode);

}
