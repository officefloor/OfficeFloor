/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.managedobject;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedObjectCleanup} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectCleanupImpl implements ManagedObjectCleanup {

	/**
	 * Avoid array creation when no {@link CleanupEscalation} instances.
	 */
	private static final CleanupEscalation[] NO_CLEANUP_ESCALATIONS = new CleanupEscalation[0];

	/**
	 * {@link ProcessState} to be cleaned up.
	 */
	private final ProcessState processState;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link CleanupEscalation} instances. Typical case is no
	 * {@link CleanupEscalation} instances, so only increase memory if have a
	 * {@link CleanupEscalation}.
	 */
	private List<CleanupEscalation> cleanupEscalations = new ArrayList<>(0);

	/**
	 * Instantiate.
	 * 
	 * @param processState   {@link ProcessState} to be cleaned up.
	 * @param officeMetaData {@link OfficeMetaData}.
	 */
	public ManagedObjectCleanupImpl(ProcessState processState, OfficeMetaData officeMetaData) {
		this.processState = processState;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ==================== CleanupSequence ============================
	 */

	@Override
	public FunctionState cleanup(final FlowMetaData recycleFlowMetaData, final Class<?> objectType,
			final ManagedObject managedObject, final ManagedObjectPool managedObjectPool) {

		// Determine if recycling the managed object
		if (recycleFlowMetaData == null) {
			return null; // no clean up
		}

		// Clean up the managed object
		return new CleanupOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) {

				// Create the recycle managed object parameter
				RecycleManagedObjectParameterImpl<ManagedObject> parameter = new RecycleManagedObjectParameterImpl<ManagedObject>(
						objectType, managedObject, managedObjectPool);

				// Obtain the recycle thread state
				ThreadState recycleThreadState = ManagedObjectCleanupImpl.this.processState.getMainThreadState();

				// Create the recycle function
				FunctionState recycleFunction = ManagedObjectCleanupImpl.this.officeMetaData
						.createProcess(recycleFlowMetaData, parameter, parameter, recycleThreadState);

				// Run recycle function in main thread state
				return new RunInThreadStateFunctionState(recycleFunction, recycleThreadState);
			}
		};
	}

	/**
	 * Runs the {@link FunctionState} and all its subsequent {@link FunctionState}
	 * instances in the specified {@link ThreadState}.
	 */
	private class RunInThreadStateFunctionState extends AbstractDelegateFunctionState {

		/**
		 * Override {@link ThreadState}.
		 */
		private final ThreadState overrideThreadState;

		/**
		 * Instantiate.
		 * 
		 * @param delegate            Delegate {@link FunctionState}.
		 * @param overrideThreadState Override {@link ThreadState}.
		 */
		public RunInThreadStateFunctionState(FunctionState delegate, ThreadState overrideThreadState) {
			super(delegate);
			this.overrideThreadState = overrideThreadState;
		}

		@Override
		public ThreadState getThreadState() {
			return this.overrideThreadState;
		}

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {

			// Execute the delegate
			FunctionState next = this.delegate.execute(context);

			// If same thread state, continue override
			if ((next != null) && (this.delegate.getThreadState() == next.getThreadState())) {
				return new RunInThreadStateFunctionState(next, this.overrideThreadState);
			}

			// Spawned to different thread state, so no further override
			return next;
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			// Delegate handle escalation (ultimately by flow callback)
			ThreadState threadState = this.delegate.getThreadState();
			return threadState.handleEscalation(escalation, completion);
		}
	}

	/**
	 * Clean up operation.
	 */
	private abstract class CleanupOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectCleanupImpl.this.processState.getMainThreadState();
		}
	}

	/**
	 * Implementation of {@link RecycleManagedObjectParameter}.
	 */
	private class RecycleManagedObjectParameterImpl<MO extends ManagedObject>
			extends AbstractLinkedListSetEntry<FlowCompletion, ManagedFunctionContainer>
			implements RecycleManagedObjectParameter<MO>, FlowCallback {

		/**
		 * Type of the object for the {@link ManagedObject}.
		 */
		private final Class<?> objectType;

		/**
		 * {@link ManagedObject} being recycled.
		 */
		private final MO managedObject;

		/**
		 * {@link ManagedObjectPool}.
		 */
		private final ManagedObjectPool pool;

		/**
		 * Flag indicating if has been recycled.
		 */
		private boolean isRecycled = false;

		/**
		 * Initiate.
		 * 
		 * @param objectType    Type of the object for the {@link ManagedObject}.
		 * @param managedObject {@link ManagedObject} to recycle.
		 * @param pool          {@link ManagedObjectPool}.
		 */
		private RecycleManagedObjectParameterImpl(Class<?> objectType, MO managedObject, ManagedObjectPool pool) {
			this.objectType = objectType;
			this.managedObject = managedObject;
			this.pool = pool;
		}

		/*
		 * ================= LinkedListSetEntry ==============================
		 */

		@Override
		public ManagedFunctionContainer getLinkedListSetOwner() {
			throw new IllegalStateException("Should never be added to a list");
		}

		/*
		 * ============= RecycleManagedObjectParameter =======================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public MO getManagedObject() {
			// Ensure provide source managed object to recycle
			if (this.pool != null) {
				return (MO) this.pool.getSourcedManagedObject(this.managedObject);
			} else {
				return this.managedObject;
			}
		}

		@Override
		public void reuseManagedObject() {

			// Return to pool
			if (this.pool != null) {
				this.pool.returnManagedObject(this.managedObject);
			}

			// Flag recycled
			this.isRecycled = true;
		}

		@Override
		public CleanupEscalation[] getCleanupEscalations() {
			ManagedObjectCleanupImpl cleanup = ManagedObjectCleanupImpl.this;
			if (cleanup.cleanupEscalations.size() == 0) {
				return NO_CLEANUP_ESCALATIONS;
			} else {
				return cleanup.cleanupEscalations
						.toArray(new CleanupEscalation[ManagedObjectCleanupImpl.this.cleanupEscalations.size()]);
			}
		}

		/*
		 * ==================== FlowCallback ================================
		 */

		@Override
		public void run(Throwable escalation) {

			// Add possible escalation
			if (escalation != null) {
				ManagedObjectCleanupImpl.this.cleanupEscalations
						.add(new CleanupEscalationImpl(this.objectType, escalation));
			}

			// Recycle (if not already recycled)
			if ((!this.isRecycled) && (this.pool != null)) {
				// Not recycled, therefore lost to pool
				this.pool.lostManagedObject(this.managedObject, escalation);
			}
		}
	}

	/**
	 * {@link CleanupEscalation} implementation.
	 */
	private static class CleanupEscalationImpl implements CleanupEscalation {

		/**
		 * Object type of the {@link ManagedObject}.
		 */
		private final Class<?> objectType;

		/**
		 * {@link Escalation} on cleanup of the {@link ManagedObject}.
		 */
		private final Throwable escalation;

		/**
		 * Initiate.
		 * 
		 * @param objectType Object type of the {@link ManagedObject}.
		 * @param escalation {@link Escalation} cleanup of the {@link ManagedObject}.
		 */
		public CleanupEscalationImpl(Class<?> objectType, Throwable escalation) {
			this.objectType = objectType;
			this.escalation = escalation;
		}

		/*
		 * ======================== CleanupEscalation ==========================
		 */

		@Override
		public Class<?> getObjectType() {
			return this.objectType;
		}

		@Override
		public Throwable getEscalation() {
			return this.escalation;
		}
	}

}
