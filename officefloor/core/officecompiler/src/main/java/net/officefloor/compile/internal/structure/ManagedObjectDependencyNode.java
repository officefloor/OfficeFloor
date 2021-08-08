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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;

/**
 * {@link ManagedObjectDependency} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyNode extends LinkObjectNode, SectionManagedObjectDependency,
		OfficeManagedObjectDependency, OfficeFloorManagedObjectDependency {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Dependency";

	/**
	 * Initialises the {@link ManagedObjectDependencyNode}.
	 */
	void initialise();

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
