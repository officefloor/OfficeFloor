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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionInterest;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedObjectContainer} instances bound to the
 * {@link ManagedFunctionContainer}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionBoundManagedObjects {

	/**
	 * Reduce object creation as {@link ManagedObject} instances typically bound
	 * to {@link ThreadState} and {@link ProcessState}.
	 */
	private static final ManagedObjectContainer[] NO_FUNCTION_BOUND_MANAGED_OBJECTS = new ManagedObjectContainer[0];

	/**
	 * Registered {@link ManagedFunctionInterest} instances.
	 */
	private final LinkedListSet<ManagedFunctionInterestImpl, ManagedFunctionBoundManagedObjects> registeredInterests = new StrictLinkedListSet<ManagedFunctionInterestImpl, ManagedFunctionBoundManagedObjects>() {
		@Override
		protected ManagedFunctionBoundManagedObjects getOwner() {
			return ManagedFunctionBoundManagedObjects.this;
		}
	};

	/**
	 * {@link ManagedObjectContainer} instances bound to the
	 * {@link ManagedFunctionContainer}.
	 */
	public final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link ManagedFunctionContainer}.
	 */
	public final ManagedFunctionContainerImpl<ManagedFunctionLogicMetaData> managedFunctionContainer;

	/**
	 * Instantiate.
	 * 
	 * @param managedFunctionLogic
	 *            {@link ManagedFunctionLogic}.
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the
	 *            {@link ManagedObjectContainer} instances bound to the
	 *            {@link ManagedFunctionContainer}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link ManagedFunction}.
	 * @param isEnforceGovernance
	 *            <code>true</code> to enforce {@link Governance} on
	 *            deactivation. <code>false</code> to disregard
	 *            {@link Governance} on deactivation.
	 * @param functionLogicMetaData
	 *            {@link ManagedFunctionLogicMetaData}.
	 * @param parallelOwner
	 *            Parallel owner of this {@link ManagedFunctionContainer}. May
	 *            be <code>null</code> if no owner.
	 * @param flow
	 *            {@link Flow} for the {@link ManagedFunctionContainer}.
	 * @param isUnloadManagedObjects
	 *            Indicates whether this {@link ManagedObjectContainer} is
	 *            responsible for unloading the {@link ManagedObject} instances.
	 */
	public ManagedFunctionBoundManagedObjects(ManagedFunctionLogic managedFunctionLogic,
			ManagedObjectMetaData<?>[] managedObjectMetaData, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, boolean isEnforceGovernance,
			ManagedFunctionLogicMetaData functionLogicMetaData, ManagedFunctionContainer parallelOwner, Flow flow,
			boolean isUnloadManagedObjects) {

		// Obtain the thread state
		ThreadState threadState = flow.getThreadState();

		// Load the managed object containers bound to the function
		if (managedObjectMetaData.length == 0) {
			// Reduce object creation as rarely bound to function
			this.managedObjects = NO_FUNCTION_BOUND_MANAGED_OBJECTS;

		} else {
			// Load the function bound containers (as will be used in execution)
			this.managedObjects = new ManagedObjectContainer[managedObjectMetaData.length];
			for (int i = 0; i < managedObjectMetaData.length; i++) {
				this.managedObjects[i] = new ManagedObjectContainerImpl(managedObjectMetaData[i], threadState);
			}
		}

		// Load the managed function container
		this.managedFunctionContainer = new ManagedFunctionContainerImpl<>(null, managedFunctionLogic, this,
				requiredManagedObjects, requiredGovernance, isEnforceGovernance, functionLogicMetaData, parallelOwner,
				flow, isUnloadManagedObjects);
	}

	/**
	 * Indicates if there is a {@link ManagedFunctionInterest} in the bound
	 * {@link ManagedObjectContainer} instances of the
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @return <code>true</code> if there is a registered
	 *         {@link ManagedFunctionInterest}.
	 */
	public boolean isInterest() {
		return (this.registeredInterests.getHead() != null);
	}

	/**
	 * Creates an {@link ManagedFunctionInterest} in the bound
	 * {@link ManagedObjectContainer} instances of the
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @return New {@link ManagedFunctionInterest}.
	 */
	public ManagedFunctionInterest createInterest() {
		return new ManagedFunctionInterestImpl();
	}

	/**
	 * {@link ManagedFunctionInterest} implementation.
	 */
	private class ManagedFunctionInterestImpl
			extends AbstractLinkedListSetEntry<ManagedFunctionInterestImpl, ManagedFunctionBoundManagedObjects>
			implements ManagedFunctionInterest {

		/*
		 * ======================= LinkedListSetEntry =======================
		 */

		@Override
		public ManagedFunctionBoundManagedObjects getLinkedListSetOwner() {
			return ManagedFunctionBoundManagedObjects.this;
		}

		/*
		 * ====================== ManagedFunctionInterest ===================
		 */

		@Override
		public FunctionState registerInterest() {
			return new AbstractDelegateFunctionState(ManagedFunctionBoundManagedObjects.this.managedFunctionContainer) {
				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Easy access to bindings
					ManagedFunctionBoundManagedObjects bindings = ManagedFunctionBoundManagedObjects.this;

					// Register the interest
					bindings.registeredInterests.addEntry(ManagedFunctionInterestImpl.this);

					// Nothing further
					return null;
				}
			};
		}

		@Override
		public FunctionState unregisterInterest() {
			return new AbstractDelegateFunctionState(ManagedFunctionBoundManagedObjects.this.managedFunctionContainer) {
				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Easy access to bindings
					ManagedFunctionBoundManagedObjects bindings = ManagedFunctionBoundManagedObjects.this;

					// Unregister the interest
					if (bindings.registeredInterests.removeEntry(ManagedFunctionInterestImpl.this)) {
						// Last interest removed, so clean up managed objects
						return bindings.managedFunctionContainer.cleanUpManagedObjects();
					}

					// Further interest, so nothing further
					return null;
				}
			};
		}
	}

}