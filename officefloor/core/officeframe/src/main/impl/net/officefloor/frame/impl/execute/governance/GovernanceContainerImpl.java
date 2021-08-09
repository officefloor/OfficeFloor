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

package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectReadyCheckImpl;
import net.officefloor.frame.internal.structure.BlockState;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionInterest;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.RegisteredGovernance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link GovernanceContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerImpl<E, F extends Enum<F>> implements GovernanceContainer<E> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<E, F> metaData;

	/**
	 * {@link ThreadState} this {@link Governance} resides within.
	 */
	private final ThreadState threadState;

	/**
	 * Index of this {@link Governance} within the {@link ThreadState}.
	 */
	private final int governanceIndex;

	/**
	 * {@link RegisteredGovernance} instances.
	 */
	private final LinkedListSet<RegisteredGovernanceEntry, GovernanceContainer<E>> registeredGovernances = new StrictLinkedListSet<RegisteredGovernanceEntry, GovernanceContainer<E>>() {
		@Override
		protected GovernanceContainer<E> getOwner() {
			return GovernanceContainerImpl.this;
		}
	};

	/**
	 * {@link Governance}.
	 */
	private Governance<? super E, F> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData        {@link GovernanceMetaData}.
	 * @param threadState     {@link ThreadState}.
	 * @param governanceIndex Index of the {@link Governance} within the
	 *                        {@link ThreadState}.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<E, F> metaData, ThreadState threadState, int governanceIndex) {
		this.metaData = metaData;
		this.threadState = threadState;
		this.governanceIndex = governanceIndex;
	}

	/**
	 * Undertakes the {@link GovernanceActivity}.
	 * 
	 * @param isWaitManagedObjectsReady <code>true</code> to wait on the
	 *                                  {@link ManagedObject} instances under
	 *                                  {@link Governance} to be ready.
	 * @param activity                  {@link GovernanceActivity}.
	 * @return {@link FunctionState} to undertake the {@link GovernanceActivity}.
	 */
	private BlockState doGovernanceActivity(boolean isWaitManagedObjectsReady, GovernanceActivity<F> activity) {
		return new GovernanceBlockOperation() {

			/**
			 * Indicates if the {@link GovernanceActivity} is loaded.
			 */
			private ManagedFunctionContainer governanceActivityContainer = null;

			/**
			 * {@link ManagedObjectReadyCheck}.
			 */
			private ManagedObjectReadyCheckImpl check = null;

			@Override
			public FunctionState execute(FunctionStateContext context) {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Create the governance activity in its own flow
				if (this.governanceActivityContainer == null) {
					Flow flow = container.threadState.createFlow(null, null);
					this.governanceActivityContainer = flow.createGovernanceFunction(activity, container.metaData);
					this.loadSequentialBlock(this.governanceActivityContainer);
				}

				// Determine if check managed objects are ready
				if (isWaitManagedObjectsReady) {
					if (this.check == null) {
						// Undertake check to ensure managed objects are ready
						this.check = new ManagedObjectReadyCheckImpl(this, this.governanceActivityContainer);
						FunctionState checkFunction = null;
						RegisteredGovernanceEntry entry = container.registeredGovernances.getHead();
						while (entry != null) {
							RegisteredGovernanceImpl registered = entry.registeredGovernance;
							checkFunction = Promise.then(checkFunction, registered.managedObjectMetaData.checkReady(
									registered.managedFunction, this.check, registered.managedObjectContainer));
							entry = entry.getNext();
						}
						if (checkFunction != null) {
							return Promise.then(checkFunction, this);
						}
					} else if (!this.check.isReady()) {
						// Not ready so wait on latch release and try again
						this.check = null;
						return null;
					}
				}

				// Return the governance function
				return this.getNextBlockToExecute();
			}
		};
	}

	/*
	 * ==================== GovernanceContainer =========================
	 */

	@Override
	public boolean isGovernanceActive() {
		return (this.governance != null);
	}

	@Override
	public <O extends Enum<O>> RegisteredGovernance registerManagedObject(E managedObjectExtension,
			ManagedObjectContainer managedobjectContainer, ManagedObjectMetaData<O> managedObjectMetaData,
			ManagedFunctionContainer managedFunctionContainer) {
		return new RegisteredGovernanceEntry(managedObjectExtension, managedobjectContainer, managedObjectMetaData,
				managedFunctionContainer).registeredGovernance;
	}

	/**
	 * {@link RegisteredGovernance} entry.
	 */
	private class RegisteredGovernanceEntry
			extends AbstractLinkedListSetEntry<RegisteredGovernanceEntry, GovernanceContainer<E>> {

		/**
		 * {@link RegisteredGovernanceImpl}.
		 */
		private final RegisteredGovernanceImpl registeredGovernance;

		/**
		 * Instantiate.
		 * 
		 * @param registeredGovernance   {@link RegisteredGovernanceImpl}.
		 * @param managedObjectContainer {@link ManagedObjectContainer} for the
		 *                               {@link ManagedObject}.
		 * @param managedObjectMetaData  {@link ManagedFunctionMetaData} for the
		 *                               {@link ManagedObjectContainer}.
		 * @param managedFunction        {@link ManagedFunctionContainer} to access
		 *                               dependencies.
		 */
		public RegisteredGovernanceEntry(E managedObjectExtension, ManagedObjectContainer managedObjectContainer,
				ManagedObjectMetaData<?> managedObjectMetaData, ManagedFunctionContainer managedFunction) {
			this.registeredGovernance = new RegisteredGovernanceImpl(this, managedObjectExtension,
					managedObjectContainer, managedObjectMetaData, managedFunction);
		}

		@Override
		public GovernanceContainer<E> getLinkedListSetOwner() {
			return GovernanceContainerImpl.this;
		}
	}

	/**
	 * {@link RegisteredGovernance} implementation.
	 */
	private class RegisteredGovernanceImpl extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements RegisteredGovernance {

		/**
		 * {@link RegisteredGovernanceEntry}.
		 */
		private final RegisteredGovernanceEntry entry;

		/**
		 * Extension to the {@link ManagedObject} to enable {@link Governance}.
		 */
		private final E managedObjectExtension;

		/**
		 * {@link ManagedObjectContainer}.
		 */
		private final ManagedObjectContainer managedObjectContainer;

		/**
		 * {@link ManagedObjectMetaData} for the {@link ManagedObjectContainer}.
		 */
		private final ManagedObjectMetaData<?> managedObjectMetaData;

		/**
		 * {@link ManagedFunctionContainer} to access dependencies.
		 */
		private final ManagedFunctionContainer managedFunction;

		/**
		 * {@link ManagedFunctionInterest}.
		 */
		private ManagedFunctionInterest interest = null;

		/**
		 * Instantiate.
		 * 
		 * @param entry                  {@link RegisteredGovernanceEntry}.
		 * @param managedObjectExtension Extension to the {@link ManagedObject} to
		 *                               enable {@link Governance}.
		 * @param managedObjectContainer {@link ManagedObjectContainer} for the
		 *                               {@link ManagedObject}.
		 * @param managedObjectMetaData  {@link ManagedFunctionMetaData} for the
		 *                               {@link ManagedObjectContainer}.
		 * @param managedFunction        {@link ManagedFunctionContainer} to access
		 *                               dependencies.
		 */
		public RegisteredGovernanceImpl(RegisteredGovernanceEntry entry, E managedObjectExtension,
				ManagedObjectContainer managedObjectContainer, ManagedObjectMetaData<?> managedObjectMetaData,
				ManagedFunctionContainer managedFunction) {
			this.entry = entry;
			this.managedObjectExtension = managedObjectExtension;
			this.managedObjectContainer = managedObjectContainer;
			this.managedObjectMetaData = managedObjectMetaData;
			this.managedFunction = managedFunction;
		}

		/*
		 * ======================= FunctionState ===============================
		 */

		@Override
		public TeamManagement getResponsibleTeam() {
			return GovernanceContainerImpl.this.metaData.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return GovernanceContainerImpl.this.threadState;
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to registered governance
			RegisteredGovernanceImpl registeredGovernance = RegisteredGovernanceImpl.this;

			// Easy access to container
			GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

			// Register the governance
			container.registeredGovernances.addEntry(this.entry);

			// Keep function dependencies available
			registeredGovernance.interest = registeredGovernance.managedFunction.createInterest();

			// Determine if must be activated in existing governance
			if (container.governance == null) {
				// Not active governance, so just register interest
				return registeredGovernance.interest.registerInterest();
			}

			// Must activate managed object with governance
			return container.doGovernanceActivity(false, new GovernanceActivity<F>() {
				@Override
				public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {

					// Govern the managed object
					container.governance.governManagedObject(RegisteredGovernanceImpl.this.managedObjectExtension,
							context);

					// Register interest in function
					return registeredGovernance.interest.registerInterest();
				}
			});
		}
	}

	@Override
	public BlockState activateGovernance() {
		return this.doGovernanceActivity(true, new GovernanceActivity<F>() {
			@Override
			public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Ensure have governance
				if (container.governance == null) {
					container.governance = container.metaData.getGovernanceFactory().createGovernance();
				}

				// Activate governance for each registered managed object
				RegisteredGovernanceEntry registered = container.registeredGovernances.getHead();
				while (registered != null) {
					container.governance.governManagedObject(registered.registeredGovernance.managedObjectExtension,
							context);
					registered = registered.getNext();
				}

				// Governance activated
				return null;
			}
		});
	}

	@Override
	public BlockState enforceGovernance() {
		return this.doGovernanceActivity(true, new GovernanceActivity<F>() {
			@Override
			public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Enforce the governance
				Governance<? super E, F> governance = container.governance;
				container.governance = null;
				governance.enforceGovernance(context);

				// Governance enforced
				return null;
			}
		});
	}

	@Override
	public BlockState disregardGovernance() {
		return this.doGovernanceActivity(true, new GovernanceActivity<F>() {
			@Override
			public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Disregard the governance
				Governance<? super E, F> governance = container.governance;
				container.governance = null;
				governance.disregardGovernance(context);

				// Governance disregarded
				return null;
			}
		});
	}

	@Override
	public FunctionState deactivateGovernance() {
		return new GovernanceOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Unregistered the managed objects
				return LinkedListSetPromise.purge(container.registeredGovernances, (governance) -> {

					// Unregister the interest in the function
					FunctionState unregisterFunction = null;
					if (governance.registeredGovernance.interest != null) {
						unregisterFunction = governance.registeredGovernance.interest.unregisterInterest();
					}

					// Unregister the managed object
					FunctionState unregisterManagedObject = governance.registeredGovernance.managedObjectContainer
							.unregisterGovernance(container.governanceIndex);

					// Undertake unregistering
					return Promise.then(unregisterFunction, unregisterManagedObject);
				});
			}
		};
	}

	/**
	 * {@link Governance} operation.
	 */
	private abstract class GovernanceOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public TeamManagement getResponsibleTeam() {
			return GovernanceContainerImpl.this.metaData.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return GovernanceContainerImpl.this.threadState;
		}
	}

	/**
	 * {@link Governance} {@link BlockState} operation.
	 */
	private abstract class GovernanceBlockOperation extends GovernanceOperation implements BlockState {

		/**
		 * Parallel owner.
		 */
		private BlockState parallelOwner;

		/**
		 * Parallel {@link BlockState}.
		 */
		private BlockState parallelBlock;

		/**
		 * Sequential {@link BlockState}.
		 */
		private BlockState sequentialBlock;

		/**
		 * ===================== BlockState ==========================
		 */

		@Override
		public void setParallelOwner(BlockState parallelOwner) {
			this.parallelOwner = parallelOwner;
		}

		@Override
		public BlockState getParallelOwner() {
			return this.parallelOwner;
		}

		@Override
		public void setParallelBlock(BlockState parallelBlock) {
			this.parallelBlock = parallelBlock;
		}

		@Override
		public BlockState getParallelBlock() {
			return this.parallelBlock;
		}

		@Override
		public void setSequentialBlock(BlockState sequentialBlock) {
			this.sequentialBlock = sequentialBlock;
		}

		@Override
		public BlockState getSequentialBlock() {
			return this.sequentialBlock;
		}
	}

}
