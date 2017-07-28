/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.execution;

import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.impl.execute.pool.ManagedObjectPoolContextImpl;
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
	 * {@link ThreadCompletionListener} instances.
	 */
	private final ThreadCompletionListener[] threadCompletionListeners;

	/**
	 * Instantiate.
	 * 
	 * @param threadCompletionListeners
	 *            {@link ThreadCompletionListener} instances.
	 */
	public ManagedExecutionFactoryImpl(ThreadCompletionListener[] threadCompletionListeners) {
		this.threadCompletionListeners = threadCompletionListeners;
	}

	/*
	 * ======================== ManagedExecutionFactory ========================
	 */

	@Override
	public <E extends Throwable> ManagedExecution<E> createManagedExecution(Execution<E> execution) {
		return new ManagedExecutionImpl<>(execution);
	}

	/**
	 * {@link ManagedExecution} implementation.
	 */
	private class ManagedExecutionImpl<E extends Throwable> implements ManagedExecution<E> {

		/**
		 * {@link Execution}.
		 */
		private final Execution<E> execution;

		/**
		 * Instantiate.
		 * 
		 * @param execution
		 *            {@link Execution}.
		 */
		public ManagedExecutionImpl(Execution<E> execution) {
			this.execution = execution;
		}

		/*
		 * ====================== ManagedExecution ==========================
		 */

		@Override
		public void execute() throws E {
			try {
				// Flag that a managed thread
				ManagedObjectPoolContextImpl.flagCurrentThreadManaged();

				// Undertake execution
				this.execution.execute();

			} finally {
				// Notify that thread management is complete
				for (int i = 0; i < ManagedExecutionFactoryImpl.this.threadCompletionListeners.length; i++) {
					ManagedExecutionFactoryImpl.this.threadCompletionListeners[i].threadComplete();
				}
			}
		}
	}

}