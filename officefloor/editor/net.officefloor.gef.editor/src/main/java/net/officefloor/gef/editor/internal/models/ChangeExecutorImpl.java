/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor.internal.models;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.ChangeListener;
import net.officefloor.gef.editor.AdaptedErrorHandler.MessageOnlyException;
import net.officefloor.gef.editor.AdaptedErrorHandler.UncertainOperation;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * {@link Change} executor.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeExecutorImpl implements ChangeExecutor {

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private final OfficeFloorContentPartFactory<?, ?> contentPartFactory;

	/**
	 * {@link IDomain}.
	 */
	private final IDomain domain;

	/**
	 * Registered {@link ChangeListener} instances.
	 */
	private final List<ChangeListener> changeListeners = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contentPartFactory {@link OfficeFloorContentPartFactory}.
	 * @param domain             {@link IDomain}.
	 */
	public ChangeExecutorImpl(OfficeFloorContentPartFactory<?, ?> contentPartFactory, IDomain domain) {
		this.contentPartFactory = contentPartFactory;
		this.domain = domain;
	}

	/*
	 * =============== ChangeExecutor ====================
	 */

	@Override
	public void execute(Change<?> change) {
		this.contentPartFactory.getErrorHandler().isError(() -> {

			// Determine if able to apply
			if (!change.canApply()) {

				// Unable to apply, so throw appropriate exception
				StringBuilder message = new StringBuilder();
				boolean isFirst = true;
				for (Conflict conflict : change.getConflicts()) {

					// Determine if cause
					Throwable cause = conflict.getConflictCause();
					if (cause != null) {
						throw cause;
					}

					// Construct the message
					if (!isFirst) {
						message.append(System.lineSeparator());
					}
					isFirst = false;
					String description = conflict.getConflictDescription();
					if ((description == null) || (description.trim().length() == 0)) {
						description = "Conflict in making change";
					}
					message.append(description);
				}
				throw new MessageOnlyException(message.toString());
			}

			// Register executing the change (executes the change)
			this.domain.execute(new ChangeTransactionalOperation(change), null);
		});
	}

	@Override
	public void execute(ITransactionalOperation operation) {
		this.contentPartFactory.getErrorHandler().isError(() -> {

			// Notify before transaction operation
			for (ChangeListener listener : this.changeListeners) {
				listener.beforeTransactionOperation(operation);
			}

			// Execute operation
			this.domain.execute(operation, null);

			// Notify after transaction operation
			for (ChangeListener listener : this.changeListeners) {
				listener.afterTransactionOperation(operation);
			}
		});
	}

	@Override
	public void addChangeListener(ChangeListener changeListener) {
		if (!this.changeListeners.contains(changeListener)) {
			this.changeListeners.add(changeListener);
		}
	}

	@Override
	public void removeChangeListener(ChangeListener changeListener) {
		this.changeListeners.remove(changeListener);
	}

	/**
	 * {@link ITransactionalOperation} for the change.
	 */
	private class ChangeTransactionalOperation extends AbstractOperation implements ITransactionalOperation {

		/**
		 * {@link Change}.
		 */
		private final Change<?> change;

		/**
		 * Instantiate.
		 * 
		 * @param change {@link Change}.
		 */
		private ChangeTransactionalOperation(Change<?> change) {
			super(change.getChangeDescription());
			this.change = change;
		}

		/**
		 * Runs the {@link UncertainOperation}.
		 * 
		 * @param operation {@link UncertainOperation}.
		 * @return Appropriate {@link IStatus} based on {@link UncertainOperation}.
		 */
		private IStatus runUncertain(UncertainOperation operation) {
			boolean isError = ChangeExecutorImpl.this.contentPartFactory.getErrorHandler().isError(operation);
			return isError ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		/**
		 * Applies the {@link Change}.
		 * 
		 * @return {@link IStatus}.
		 */
		private IStatus apply() {
			return this.runUncertain(() -> {

				// Notify pre-apply
				for (ChangeListener listener : ChangeExecutorImpl.this.changeListeners) {
					listener.preApply(this.change);
				}

				// Apply change
				this.change.apply();

				// Notify post-apply
				for (ChangeListener listener : ChangeExecutorImpl.this.changeListeners) {
					listener.postApply(this.change);
				}
			});
		}

		/**
		 * Reverts the {@link Change}.
		 * 
		 * @return {@link IStatus}.
		 */
		private IStatus revert() {
			return this.runUncertain(() -> {

				// Notify pre-revert
				for (ChangeListener listener : ChangeExecutorImpl.this.changeListeners) {
					listener.preRevert(this.change);
				}

				// Revert change
				this.change.revert();

				// Notify post-revert
				for (ChangeListener listener : ChangeExecutorImpl.this.changeListeners) {
					listener.postRevert(this.change);
				}
			});
		}

		/*
		 * =============== ITransactionalOperation =====================
		 */

		@Override
		public boolean isContentRelevant() {
			return true;
		}

		@Override
		public boolean isNoOp() {
			return false;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.apply();
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.revert();
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.apply();
		}
	}

}
