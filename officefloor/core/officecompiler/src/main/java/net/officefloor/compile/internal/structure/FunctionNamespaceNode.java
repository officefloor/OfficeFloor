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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;

/**
 * {@link SectionFunctionNamespace} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceNode extends Node, SectionFunctionNamespace {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Function Namespace";

	/**
	 * Initialises the {@link FunctionNamespaceNode}.
	 * 
	 * @param managedFunctionSourceClassName {@link Class} name of the
	 *                                       {@link ManagedFunctionSource}.
	 * @param managedFunctionSource          Optional instantiated
	 *                                       {@link ManagedFunctionSource}. May be
	 *                                       <code>null</code>.
	 */
	void initialise(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource);

	/**
	 * Obtains the {@link SectionNode} containing this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link FunctionNamespaceNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the {@link FunctionNamespaceType} for this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @param isLoadingType Indicates using to load type.
	 * @return {@link FunctionNamespaceType} for this {@link FunctionNamespaceNode}.
	 *         May be <code>null</code> if can not load the
	 *         {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType loadFunctionNamespaceType(boolean isLoadingType);

	/**
	 * Registers the {@link ManagedFunctionSource} as a possible MBean.
	 * 
	 * @param compileContext {@link CompileContext}.
	 */
	void registerAsPossibleMbean(CompileContext compileContext);

}
