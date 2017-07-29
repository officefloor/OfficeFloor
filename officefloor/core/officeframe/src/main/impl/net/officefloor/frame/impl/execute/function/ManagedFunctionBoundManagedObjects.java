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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionInterest;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
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
	public final ManagedFunctionContainerImpl<ManagedFunctionMetaData<?, ?>> managedFunctionContainer;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the
	 *            {@link ManagedObjectContainer} instances bound to the
	 *            {@link ManagedFunctionContainer}.
	 * @param threadState
	 *            {@link ThreadState} for the {@link ManagedFunctionContainer}.
	 */
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBoundManagedObjects(Object parameter,
			ManagedFunctionMetaData<O, F> managedFunctionMetaData, boolean isEnforceGovernance,
			ManagedFunctionContainer parallelOwner, boolean isUnloadManagedObjects, Flow flow) {

		// Obtain the thread state
		ThreadState threadState = flow.getThreadState();

		// Load the managed object containers bound to the function
		ManagedObjectMetaData<?>[] managedObjectMetaData = managedFunctionMetaData.getManagedObjectMetaData();
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
		ManagedFunctionLogic managedFunctionLogic = new ManagedFunctionLogicImpl<>(managedFunctionMetaData, parameter);
		this.managedFunctionContainer = new ManagedFunctionContainerImpl<ManagedFunctionMetaData<?, ?>>(null,
				managedFunctionLogic, this, managedFunctionMetaData.getRequiredManagedObjects(),
				managedFunctionMetaData.getRequiredGovernance(), isEnforceGovernance, managedFunctionMetaData,
				parallelOwner, flow, isUnloadManagedObjects);
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