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
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectIndex} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectIndexImpl implements ManagedObjectIndex {

	/**
	 * {@link ManagedObjectScope}.
	 */
	private final ManagedObjectScope managedObjectScope;

	/**
	 * Index of the {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	private final int indexOfManagedObjectWithinScope;

	/**
	 * Initiate.
	 *
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param indexOfManagedObjectWithinScope
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public ManagedObjectIndexImpl(ManagedObjectScope managedObjectScope,
			int indexOfManagedObjectWithinScope) {
		this.managedObjectScope = managedObjectScope;
		this.indexOfManagedObjectWithinScope = indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= ManagedObjectIndex ===========================
	 */

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	@Override
	public int getIndexOfManagedObjectWithinScope() {
		return this.indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public String toString() {
		// Provide details of index
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append("[");
		text.append(this.managedObjectScope.toString());
		text.append(":");
		text.append(this.indexOfManagedObjectWithinScope);
		text.append("]");
		return text.toString();
	}

}
