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
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Node representing instance use of a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolNode
		extends LinkPoolNode, OfficeFloorManagedObjectPool, OfficeManagedObjectPool, SectionManagedObjectPool {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object Pool";

	/**
	 * Initialises the {@link ManagedObjectPoolNode}.
	 * 
	 * @param managedObjectPoolSourceClassName Class name of the
	 *                                         {@link ManagedObjectPoolSource}.
	 * @param managedObjectPoolSource          Optional instantiated
	 *                                         {@link ManagedObjectPoolSource}. May
	 *                                         be <code>null</code>.
	 */
	void initialise(String managedObjectPoolSourceClassName, ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Loads the {@link ManagedObjectPoolType}.
	 * 
	 * @param isLoadingType Indicates if using to load type.
	 * @return {@link ManagedObjectPoolType} or <code>null</code> if issue loading
	 *         with issue reported to the {@link CompilerIssues}.
	 */
	ManagedObjectPoolType loadManagedObjectPoolType(boolean isLoadingType);

	/**
	 * Sources the {@link ManagedObjectPool}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise,
	 *         <code>false</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceManagedObjectPool(CompileContext compileContext);

	/**
	 * Builds {@link ManagedObjectPool} for this {@link ManagedObjectPoolNode}.
	 *
	 * @param managedObjectBuilder {@link ManagedObjectBuilder}.
	 * @param managedObjectType    {@link ManagedObjectType} of the
	 *                             {@link ManagedObjectSource} being pooled.
	 * @param compileContext       {@link CompileContext}.
	 */
	void buildManagedObjectPool(ManagedObjectBuilder<?> managedObjectBuilder, ManagedObjectType<?> managedObjectType,
			CompileContext compileContext);

}
