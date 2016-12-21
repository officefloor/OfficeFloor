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

import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObjectReadyCheck} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectReadyCheckImpl implements ManagedObjectReadyCheck {

	/**
	 * {@link ManagedFunctionContainer} requiring the check.
	 */
	private final ManagedFunctionContainer managedJobNode;

	/**
	 * Flag indicating if ready.
	 */
	private boolean isReady = true;

	/**
	 * Instantiate.
	 * 
	 * @param managedJobNode
	 *            {@link ManagedFunctionContainer} requiring the check.
	 */
	public ManagedObjectReadyCheckImpl(ManagedFunctionContainer managedJobNode) {
		this.managedJobNode = managedJobNode;
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
	public ManagedFunctionContainer getManagedJobNode() {
		return this.managedJobNode;
	}

	@Override
	public FunctionState setNotReady() {
		return new NotReadyJobNode();
	}

	/**
	 * Flags that the {@link ManagedObject} is not ready.
	 */
	private class NotReadyJobNode implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectReadyCheckImpl.this.managedJobNode.getThreadState();
		}

		@Override
		public FunctionState execute() {
			ManagedObjectReadyCheckImpl.this.isReady = false;
			return null;
		}
	}

}
