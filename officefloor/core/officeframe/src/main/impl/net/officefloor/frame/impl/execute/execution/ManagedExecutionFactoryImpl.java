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

package net.officefloor.frame.impl.execute.execution;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ManagedExecution;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;

/**
 * {@link ManagedExecutionFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedExecutionFactoryImpl implements ManagedExecutionFactory {

	/**
	 * Indicates if the current {@link Thread} is executing within a
	 * {@link ManagedExecution}.
	 * 
	 * @return <code>true</code> if current {@link Thread} is executing within a
	 *         {@link ManagedExecution}.
	 */
	public static boolean isCurrentThreadManaged() {
		return threadManagedExecutionState.get().isManaged;
	}

	/**
	 * {@link ThreadLocal} to indicate if the current {@link Thread} is managed.
	 */
	private static final ThreadLocal<ManagedExecutionState> threadManagedExecutionState = new ThreadLocal<ManagedExecutionState>() {
		@Override
		protected ManagedExecutionState initialValue() {
			return new ManagedExecutionState();
		}
	};

	/**
	 * State of {@link ManagedExecution} for the {@link Thread}.
	 */
	private static class ManagedExecutionState {

		/**
		 * Indicates if {@link Thread} is within {@link ManagedExecution}.
		 */
		private boolean isManaged = false;
	}

	/**
	 * {@link ThreadCompletionListener} instances.
	 */
	private final ThreadCompletionListener[] threadCompletionListeners;

	/**
	 * Instantiate.
	 * 
	 * @param threadCompletionListeners {@link ThreadCompletionListener} instances.
	 */
	public ManagedExecutionFactoryImpl(ThreadCompletionListener[] threadCompletionListeners) {
		this.threadCompletionListeners = threadCompletionListeners;
	}

	/*
	 * ======================== ManagedExecutionFactory ========================
	 */

	@Override
	public <E extends Throwable> ManagedExecution<E> createManagedExecution(Executive executive,
			Execution<E> execution) {
		return new ManagedExecutionImpl<>(executive, execution);
	}

	/**
	 * {@link ManagedExecution} implementation.
	 */
	private class ManagedExecutionImpl<E extends Throwable> implements ManagedExecution<E>, Execution<E> {

		/**
		 * {@link Executive}.
		 */
		private final Executive executive;

		/**
		 * {@link Execution}.
		 */
		private final Execution<E> execution;

		/**
		 * Instantiate.
		 * 
		 * @param executive {@link Executive}.
		 * @param execution {@link Execution}.
		 */
		public ManagedExecutionImpl(Executive executive, Execution<E> execution) {
			this.executive = executive;
			this.execution = execution;
		}

		/*
		 * ====================== ManagedExecution ==========================
		 */

		@Override
		public ProcessManager managedExecute() throws E {
			return this.executive.manageExecution(this);
		}

		/*
		 * ========================= Execution ===============================
		 */

		@Override
		public ProcessManager execute() throws E {

			// Determine if state of managed execution for thread
			ManagedExecutionState state = ManagedExecutionFactoryImpl.threadManagedExecutionState.get();
			boolean isAlreadyManagd = state.isManaged;

			// Undertake (potentially managed)
			try {
				// Ensure being managed
				if (!isAlreadyManagd) {
					state.isManaged = true;
				}

				// Undertake execution
				return this.execution.execute();

			} finally {
				// Only clean up if not previously managed (not internal call)
				if (!isAlreadyManagd) {
					try {
						// Notify that thread management is complete
						for (int i = 0; i < ManagedExecutionFactoryImpl.this.threadCompletionListeners.length; i++) {
							ManagedExecutionFactoryImpl.this.threadCompletionListeners[i].threadComplete();
						}
					} finally {
						// No longer managed
						state.isManaged = false;
					}
				}
			}
		}
	}

}
