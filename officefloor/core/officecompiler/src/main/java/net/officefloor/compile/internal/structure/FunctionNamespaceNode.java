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
	 * @param managedFunctionSourceClassName
	 *            {@link Class} name of the {@link ManagedFunctionSource}.
	 * @param managedFunctionSource
	 *            Optional instantiated {@link ManagedFunctionSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource);

	/**
	 * Obtains the {@link SectionNode} containing this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @return {@link SectionNode} containing this
	 *         {@link FunctionNamespaceNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the {@link FunctionNamespaceType} for this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @return {@link FunctionNamespaceType} for this
	 *         {@link FunctionNamespaceNode}. May be <code>null</code> if can
	 *         not load the {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType loadFunctionNamespaceType();

	/**
	 * Registers the {@link ManagedFunctionSource} as a possible MBean.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void registerAsPossibleMbean(CompileContext compileContext);

}
