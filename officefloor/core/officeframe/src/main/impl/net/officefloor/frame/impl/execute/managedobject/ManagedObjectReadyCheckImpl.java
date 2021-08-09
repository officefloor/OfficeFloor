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
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedObjectReadyCheck} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectReadyCheckImpl implements ManagedObjectReadyCheck {

	/**
	 * {@link FunctionState} for the {@link AssetLatch} if not ready.
	 */
	private final FunctionState latchFunction;

	/**
	 * {@link ManagedFunctionContainer} requiring the check.
	 */
	private final ManagedFunctionContainer managedFunction;

	/**
	 * Flag indicating if ready.
	 */
	private boolean isReady = true;

	/**
	 * Instantiate.
	 * 
	 * @param latchFunction
	 *            {@link FunctionState} for the {@link AssetLatch} if not ready.
	 * @param managedFunction
	 *            {@link ManagedFunctionContainer} requiring the check.
	 */
	public ManagedObjectReadyCheckImpl(FunctionState latchFunction, ManagedFunctionContainer managedFunction) {
		this.latchFunction = latchFunction;
		this.managedFunction = managedFunction;
	}

	/**
	 * Returns whether the {@link ManagedObject} is ready.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} is ready.
	 */
	public boolean isReady() {
		return this.isReady;
	}

	/*
	 * ========================= ManagedObjectReadyCheck ======================
	 */

	@Override
	public FunctionState getLatchFunction() {
		return this.latchFunction;
	}

	@Override
	public ManagedFunctionContainer getManagedFunctionContainer() {
		return this.managedFunction;
	}

	@Override
	public FunctionState setNotReady() {
		return new NotReadyJobNode();
	}

	/**
	 * Flags that the {@link ManagedObject} is not ready.
	 */
	private class NotReadyJobNode extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectReadyCheckImpl.this.managedFunction.getThreadState();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {
			ManagedObjectReadyCheckImpl.this.isReady = false;
			return null;
		}
	}

}
