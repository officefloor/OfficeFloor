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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionContext;
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
		public FunctionState execute(FunctionContext context) {
			ManagedObjectReadyCheckImpl.this.isReady = false;
			return null;
		}
	}

}
