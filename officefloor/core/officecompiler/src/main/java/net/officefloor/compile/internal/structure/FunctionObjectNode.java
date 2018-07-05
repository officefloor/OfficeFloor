/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link FunctionObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionObjectNode extends LinkObjectNode, AugmentedFunctionObject, FunctionObject {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Function Object";

	/**
	 * Initialises the {@link FunctionObjectNode}.
	 */
	void initialise();

	/**
	 * Indicates if this {@link FunctionObject} is a parameter to the
	 * {@link ManagedFunction}.
	 * 
	 * @return <code>true</code> if this {@link FunctionObject} is a parameter
	 *         to the {@link ManagedFunction}.
	 */
	boolean isParameter();

	/**
	 * Loads the {@link ObjectDependencyType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ObjectDependencyType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	ObjectDependencyType loadObjectDependencyType(CompileContext compileContext);

}