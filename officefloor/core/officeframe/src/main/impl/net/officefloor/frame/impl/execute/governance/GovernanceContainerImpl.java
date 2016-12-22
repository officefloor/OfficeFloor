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

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.function.AbstractManagedFunctionContainer;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionContainerContext;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.RegisteredGovernance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link GovernanceContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerImpl<I, F extends Enum<F>> implements GovernanceContainer<I> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link ThreadState} this {@link Governance} resides within.
	 */
	private final ThreadState threadState;

	/**
	 * {@link RegisteredGovernance} instances.
	 */
	private final LinkedListSet<RegisteredGovernanceImpl, GovernanceContainer<I>> registeredGovernances = new StrictLinkedListSet<RegisteredGovernanceImpl, GovernanceContainer<I>>() {
		@Override
		protected GovernanceContainer<I> getOwner() {
			return GovernanceContainerImpl.this;
		}
	};

	/**
	 * {@link Governance}.
	 */
	private Governance<? super I, F> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param threadState
	 *            {@link ThreadState}.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<I, F> metaData, ThreadState threadState) {
		this.metaData = metaData;
		this.threadState = threadState;
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
				GovernanceContainerImpl<I, F> container = GovernanceContainerImpl.this;

				// Create the governance in its own flow
				Flow flow = container.threadState.createFlow();
				return new GovernanceFunction(flow, activity);
			}
		};
	}

	/*
	 * ==================== GovernanceContainer =========================
	 */

	@Override
	public RegisteredGovernance registerManagedObject(I managedObjectExtension,
			ManagedObjectContainer managedobjectContainer) {
		RegisteredGovernanceImpl registeredGovernance = new RegisteredGovernanceImpl(managedObjectExtension,
				managedobjectContainer);
		return registeredGovernance;
	}

	/**
	 * {@link RegisteredGovernance}.
	 */
	private class RegisteredGovernanceImpl
			extends AbstractLinkedListSetEntry<RegisteredGovernanceImpl, GovernanceContainer<I>>
			implements RegisteredGovernance {

		/**
		 * Extension to the {@link ManagedObject} to enable {@link Governance}.
		 */
		private final I managedObjectExtension;

		/**
		 * {@link ManagedObjectContainer}.
		 */
		private final ManagedObjectContainer managedObjectContainer;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectExtension
		 *            Extension to the {@link ManagedObject} to enable
		 *            {@link Governance}.
		 * @param managedObjectContainer
		 *            {@link ManagedObjectContainer} for the
		 *            {@link ManagedObject}.
		 */
		public RegisteredGovernanceImpl(I managedObjectExtension, ManagedObjectContainer managedObjectContainer) {
			this.managedObjectExtension = managedObjectExtension;
			this.managedObjectContainer = managedObjectContainer;
		}

		/*
		 * ====================== LinkedListSetEntry =========================
		 */

		@Override
		public GovernanceContainer<I> getLinkedListSetOwner() {
			return GovernanceContainerImpl.this;
		}

		/*
		 * ==================== RegisteredGovernance ==========================
		 */

		@Override
		public FunctionState unregisterManagedObject() {

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
			GovernanceContainerImpl<I, F> container = GovernanceContainerImpl.this;

			// Register the governance
			container.registeredGovernances.addEntry(this);

			// Determine if must be activated in existing governance
			if (container.governance == null) {
				return null; // not active governance
			}

			// Must activate managed object with governance
			return container.doGovernanceActivity(new GovernanceActivity<F>() {

			});
		}
	}

	@Override
	public FunctionState activateGovernance() {
		return this.doGovernanceActivity(new GovernanceActivity<F>() {
			@Override
			public FunctionState doActivity(GovernanceContext<F> context) throws Throwable {
				// Easy access to container
				GovernanceContainerImpl<I, F> container = GovernanceContainerImpl.this;
				try {
					// Ensure have governance
					if (container.governance == null) {
						container.governance = container.metaData.getGovernanceFactory().createGovernance();
					}

					// Activate governance for each registered managed object
					RegisteredGovernanceImpl registered = container.registeredGovernances.getHead();
					while (registered != null) {
						container.governance.governManagedObject(registered.managedObjectExtension, context);
						registered = registered.getNext();
					}

				} catch (Throwable ex) {
					container.governance = null;
					return new FailGovernanceOperation(ex);
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
				GovernanceContainerImpl<I, F> container = GovernanceContainerImpl.this;
				try {

					// Enforce the governance
					container.governance.enforceGovernance(context);

				} catch (Throwable ex) {
					return new FailGovernanceOperation(ex);
				}

				// Governance enforced
				container.governance = null;
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
				GovernanceContainerImpl<I, F> container = GovernanceContainerImpl.this;
				try {

					// Disregard the governance
					container.governance.disregardGovernance(context);

				} catch (Throwable ex) {
					return new FailGovernanceOperation(ex);
				}

				// Governance disregarded
				container.governance = null;
				return null;
			}
		});
	}

	/**
	 * {@link FunctionState} to fail the {@link Governance}.
	 */
	private class FailGovernanceOperation extends GovernanceOperation {

		/**
		 * Cause of the failure.
		 */
		private final Throwable failure;

		/**
		 * Instantiate.
		 * 
		 * @param failure
		 *            Cause of the failure.
		 */
		public FailGovernanceOperation(Throwable failure) {
			this.failure = failure;
		}

		@Override
		public FunctionState execute() {
			GovernanceContainerImpl.this.threadState.setFailure(this.failure);
			GovernanceContainerImpl.this.governance = null;
			return null;
		}
	}

	/**
	 * {@link Governance} operation.
	 */
	private abstract class GovernanceOperation implements FunctionState {

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
	 * {@link ManagedFunctionContainer} to undertake the
	 * {@link GovernanceActivity}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 */
	private class GovernanceFunction extends AbstractManagedFunctionContainer<Work, GovernanceMetaData<I, F>>
			implements GovernanceContext<F> {

		/**
		 * {@link GovernanceActivity}.
		 */
		private final GovernanceActivity<F> activity;

		/**
		 * Instantiate.
		 * 
		 * @param flow
		 *            {@link Flow}.
		 * @param workContainer
		 *            {@link WorkContainer}.
		 * @param governanceMetaData
		 *            {@link GovernanceMetaData}.
		 * @param activity
		 *            {@link GovernanceActivity}.
		 */
		public GovernanceFunction(Flow flow, GovernanceActivity<F> activity) {
			super(flow, new GovernanceWork(), GovernanceContainerImpl.this.metaData, null, null, null,
					GovernanceDeactivationStrategy.DISREGARD);
			this.activity = activity;
		}

		/*
		 * ================ ManagedFunctionContainer =======================
		 */

		@Override
		protected Object executeFunction(ManagedFunctionContainerContext context) throws Throwable {
			context.next(this.activity.doActivity(this));
			return null;
		}

		/*
		 * ================== GovernanceContext ==========================
		 */

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {

			// Obtain the flow meta-data
			FlowMetaData<?> flowMetaData = this.functionContainerMetaData.getFlow(flowIndex);

			// Undertake the flow
			this.doFlow(flowMetaData, parameter, callback);
		}
	}

}