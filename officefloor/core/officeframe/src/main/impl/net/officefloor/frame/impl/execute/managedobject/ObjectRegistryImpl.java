/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Implementation of the {@link ObjectRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectRegistryImpl<O extends Enum<O>> implements ObjectRegistry<O> {

	/**
	 * {@link ManagedFunctionContainer} to obtain the coordinating
	 * {@link ManagedObject} instances.
	 */
	private final ManagedFunctionContainer managedFunction;

	/**
	 * {@link ManagedObjectIndex} for the dependencies in the index order
	 * required.
	 */
	private final ManagedObjectIndex[] dependencies;

	/**
	 * Initiate.
	 * 
	 * @param managedFunction
	 *            {@link ManagedFunctionContainer} to obtain the coordinating
	 *            {@link ManagedObject} instances.
	 * @param dependencies
	 *            {@link ManagedObjectIndex} for the dependencies in the index
	 *            order required.
	 */
	public ObjectRegistryImpl(ManagedFunctionContainer managedFunction, ManagedObjectIndex[] dependencies) {
		this.managedFunction = managedFunction;
		this.dependencies = dependencies;
	}

	/*
	 * ===================== ObjectRegistry ===============================
	 */

	@Override
	public Object getObject(O key) {
		return this.getObject(key.ordinal());
	}

	@Override
	public Object getObject(int index) {

		// Obtain the managed object index for the dependency
		ManagedObjectIndex moIndex = this.dependencies[index];

		// Obtain the managed object container
		ManagedObjectContainer moContainer = ManagedObjectContainerImpl.getManagedObjectContainer(moIndex,
				this.managedFunction);

		// Obtain the dependency
		Object dependency = moContainer.getObject();

		// Return the dependency
		return dependency;
	}

}
