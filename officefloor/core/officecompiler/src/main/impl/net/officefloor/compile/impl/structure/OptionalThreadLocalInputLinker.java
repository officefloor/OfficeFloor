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

package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * Links the {@link OptionalThreadLocalReceiver} to the
 * {@link ManagedObjectSourceNode} for an {@link InputManagedObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalInputLinker {

	/**
	 * Listing of {@link OptionalThreadLocalReceiver} instances interested in the
	 * {@link OptionalThreadLocal}.
	 */
	private final List<OptionalThreadLocalReceiver> receivers = new LinkedList<>();

	/**
	 * {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode managedObjectSource = null;

	/**
	 * Specifies the {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode} for the
	 *                                {@link InputManagedObjectNode}.
	 */
	public void setManagedObjectSourceNode(ManagedObjectSourceNode managedObjectSourceNode) {
		this.managedObjectSource = managedObjectSourceNode;

		// Add null to trigger receiving the optional thread local
		this.addOptionalThreadLocalReceiver(null);
	}

	/**
	 * Adds an {@link OptionalThreadLocalReceiver}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	public void addOptionalThreadLocalReceiver(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {

		// Add the optional thread local receiver
		if (optionalThreadLocalReceiver != null) {
			this.receivers.add(optionalThreadLocalReceiver);
		}

		// Ensure have managed object source node
		if (this.managedObjectSource == null) {
			return; // not yet available
		}

		// Load the optional thread local receivers
		for (OptionalThreadLocalReceiver receiver : this.receivers) {
			managedObjectSource.buildSupplierThreadLocal(receiver);
		}

		// All received (so clear to avoid adding again)
		this.receivers.clear();
	}

}
