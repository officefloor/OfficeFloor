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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link ManagedObjectDependency} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyNode extends LinkObjectNode,
		ManagedObjectDependency {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Dependency";

	/**
	 * Loads the {@link ObjectDependencyType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link ObjectDependencyType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	ObjectDependencyType loadObjectDependencyType(TypeContext typeContext);

}