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

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.impl.execute.function.RunInThreadStateFunctionLogic;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;

/**
 * {@link ManagedObjectCleanup} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectCleanupImpl implements ManagedObjectCleanup {

	/**
	 * {@link ProcessState} to be cleaned up.
	 */
	private final ProcessState processState;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link CleanupEscalation} instances.
	 */
	private List<CleanupEscalation> cleanupEscalations = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param processState
	 *            {@link ProcessState} to be cleaned up.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public ManagedObjectCleanupImpl(ProcessState processState, OfficeMetaData officeMetaData) {
		this.processState = processState;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ==================== CleanupSequence ============================
	 */

	@Override
	public FunctionState createCleanUpJobNode(final FlowMetaData<?> recycleFlowMetaData, final Class<?> objectType,
			final ManagedObject managedObject, final ManagedObjectPool managedObjectPool) {
		return new CleanupOperation() {
			@Override
			public FunctionState execute() {
				if (recycleFlowMetaData == null) {
					// No recycling for managed objects
					return null;

				} else {
					// Create the recycle managed object parameter
					RecycleManagedObjectParameterImpl<ManagedObject> parameter = new RecycleManagedObjectParameterImpl<ManagedObject>(
							objectType, managedObject, managedObjectPool);
					
					// Use the recycle function responsible team for escalations
					ManagedFunctionMetaData<?, ?, ?> initialFunctionMetaData = recycleFlowMetaData.getInitialTaskMetaData();
					TeamManagement escalationResponsibleTeam = initialFunctionMetaData.getResponsibleTeam();

					// Create the recycle function
					FunctionState recycleFunction = ManagedObjectCleanupImpl.this.officeMetaData
							.createProcess(recycleFlowMetaData, parameter, parameter, escalationResponsibleTeam, parameter);

					// Run recycle job node in main thread state
					return new RunInThreadStateFunctionLogic(recycleFunction,
							ManagedObjectCleanupImpl.this.processState.getMainThreadState());
				}
			}
		};
	}

	/**
	 * Clean up operation.
	 */
	private abstract class CleanupOperation implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectCleanupImpl.this.processState.getMainThreadState();
		}
	}

	/**
	 * Implementation of {@link RecycleManagedObjectParameter}.
	 */
	private class RecycleManagedObjectParameterImpl<MO extends ManagedObject>
			implements RecycleManagedObjectParameter<MO>, EscalationHandler, ProcessCompletionListener {

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
		 * @param objectType
		 *            Type of the object for the {@link ManagedObject}.
		 * @param managedObject
		 *            {@link ManagedObject} to recycle.
		 * @param pool
		 *            {@link ManagedObjectPool}.
		 */
		private RecycleManagedObjectParameterImpl(Class<?> objectType, MO managedObject, ManagedObjectPool pool) {
			this.objectType = objectType;
			this.managedObject = managedObject;
			this.pool = pool;
		}

		/*
		 * ============= RecycleManagedObjectParameter =======================
		 */

		@Override
		public MO getManagedObject() {
			return this.managedObject;
		}

		@Override
		public void reuseManagedObject(MO managedObject) {

			// Return to pool
			if (this.pool != null) {
				this.pool.returnManagedObject(managedObject);
			}

			// Flag recycled
			this.isRecycled = true;
		}

		@Override
		public CleanupEscalation[] getCleanupEscalations() {
			return ManagedObjectCleanupImpl.this.cleanupEscalations
					.toArray(new CleanupEscalation[ManagedObjectCleanupImpl.this.cleanupEscalations.size()]);
		}

		/*
		 * ================== EscalationHandler ===============================
		 */

		@Override
		public void handleEscalation(Throwable escalation) throws Throwable {
			ManagedObjectCleanupImpl.this.cleanupEscalations
					.add(new CleanupEscalationImpl(this.objectType, escalation));
		}

		/*
		 * ============= ProcessCompletionListener ============================
		 */

		@Override
		public void processComplete() {
			if ((!this.isRecycled) && (this.pool != null)) {
				// Not recycled, therefore lost to pool
				this.pool.lostManagedObject(this.managedObject);
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
		 * @param objectType
		 *            Object type of the {@link ManagedObject}.
		 * @param escalation
		 *            {@link Escalation} cleanup of the {@link ManagedObject}.
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