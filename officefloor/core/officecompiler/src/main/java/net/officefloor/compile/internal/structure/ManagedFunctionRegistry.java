/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	 * Obtains the {@link ManagedFunctionNode} from the registry.
	 * <p>
	 * The returned {@link ManagedFunctionNode} may or may not be initialised.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunctionNode} to obtain.
	 * @return {@link ManagedFunctionNode} from the registry.
	 */
	ManagedFunctionNode getManagedFunctionNode(String functionName);

	/**
	 * <p>
	 * Adds an initialised {@link ManagedFunctionNode} to the registry.
	 * <p>
	 * Should an {@link ManagedFunctionNode} already be added by the name, then
	 * an issue is reported to the {@link CompilerIssue}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunctionNode}.
	 * @param functionTypeName
	 *            Type name of the {@link ManagedFunction} within the
	 *            {@link ManagedFunctionSource}.
	 * @param functionNamspaceNode
	 *            Parent {@link FunctionNamespaceNode}.
	 * @return Initialised {@link ManagedFunctionNode} by the name.
	 */
	ManagedFunctionNode addManagedFunctionNode(String functionName, String functionTypeName,
			FunctionNamespaceNode functionNamespaceNode);

}