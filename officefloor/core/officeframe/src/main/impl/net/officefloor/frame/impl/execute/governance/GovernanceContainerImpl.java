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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
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
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param threadState
	 *            {@link ThreadState}.
	 * @param governanceIndex
	 *            Index of the {@link Governance} within the
	 *            {@link ThreadState}.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<E, F> metaData, ThreadState threadState, int governanceIndex) {
		this.metaData = metaData;
		this.threadState = threadState;
		this.governanceIndex = governanceIndex;
	}

	/**
	 * Undertakes the {@link GovernanceActivity}.
	 * 
	 * @param activity
	 *            {@link GovernanceActivity}.
	 * @return {@link FunctionState} to undertake the
	 *         {@link GovernanceActivity}.
	 */
	private FunctionState doGovernanceActivity(GovernanceActivity<F> activity) {
		return new GovernanceOperation() {
			@Override
			public FunctionState execute() {
				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Create the governance activity in its own flow
				Flow flow = container.threadState.createFlow(null);
				return flow.createGovernanceFunction(activity, container.metaData);
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
	public RegisteredGovernance registerManagedObject(E managedObjectExtension,
			ManagedObjectContainer managedobjectContainer) {
		return new RegisteredGovernanceEntry(managedObjectExtension, managedobjectContainer).registeredGovernance;
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
		 * @param registeredGovernance
		 *            {@link RegisteredGovernanceImpl}.
		 */
		public RegisteredGovernanceEntry(E managedObjectExtension, ManagedObjectContainer managedObjectContainer) {
			this.registeredGovernance = new RegisteredGovernanceImpl(this, managedObjectExtension,
					managedObjectContainer);
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
		 * Instantiate.
		 * 
		 * @param entry
		 *            {@link RegisteredGovernanceEntry}.
		 * @param managedObjectExtension
		 *            Extension to the {@link ManagedObject} to enable
		 *            {@link Governance}.
		 * @param managedObjectContainer
		 *            {@link ManagedObjectContainer} for the
		 *            {@link ManagedObject}.
		 */
		public RegisteredGovernanceImpl(RegisteredGovernanceEntry entry, E managedObjectExtension,
				ManagedObjectContainer managedObjectContainer) {
			this.entry = entry;
			this.managedObjectExtension = managedObjectExtension;
			this.managedObjectContainer = managedObjectContainer;
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
		public FunctionState execute() {

			// Easy access to container
			GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

			// Register the governance
			container.registeredGovernances.addEntry(this.entry);

			// Determine if must be activated in existing governance
			if (container.governance == null) {
				return null; // not active governance
			}

			// Must activate managed object with governance
			return container.doGovernanceActivity(new GovernanceActivity<F>() {
				@Override
				public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {
					container.governance.governManagedObject(RegisteredGovernanceImpl.this.managedObjectExtension,
							context);
					return null;
				}
			});
		}
	}

	@Override
	public FunctionState activateGovernance() {
		return this.doGovernanceActivity(new GovernanceActivity<F>() {
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
	public FunctionState enforceGovernance() {
		return this.doGovernanceActivity(new GovernanceActivity<F>() {
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
	public FunctionState disregardGovernance() {
		return this.doGovernanceActivity(new GovernanceActivity<F>() {
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
			public FunctionState execute() throws Throwable {

				// Easy access to container
				GovernanceContainerImpl<E, F> container = GovernanceContainerImpl.this;

				// Unregistered the managed objects
				return LinkedListSetPromise.purge(container.registeredGovernances,
						(governance) -> governance.registeredGovernance.managedObjectContainer
								.unregisterGovernance(container.governanceIndex));
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

}