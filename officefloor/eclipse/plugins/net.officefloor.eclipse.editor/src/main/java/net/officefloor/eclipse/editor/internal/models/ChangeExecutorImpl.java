/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor.internal.models;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.eclipse.editor.AdaptedErrorHandler.UncertainOperation;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.change.Change;

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
	 * Instantiate.
	 * 
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 * @param domain
	 *            {@link IDomain}.
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
		this.contentPartFactory.getErrorHandler()
				.isError(() -> this.domain.execute(new ChangeTransactionalOperation(change), null));
	}

	@Override
	public void execute(ITransactionalOperation operation) {
		this.contentPartFactory.getErrorHandler().isError(() -> this.domain.execute(operation, null));
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
		 * @param change
		 *            {@link Change}.
		 */
		private ChangeTransactionalOperation(Change<?> change) {
			super(change.getChangeDescription());
			this.change = change;
		}

		/**
		 * Runs the {@link UncertainOperation}.
		 * 
		 * @param operation
		 *            {@link UncertainOperation}.
		 * @return Appropriate {@link IStatus} based on {@link UncertainOperation}.
		 */
		private IStatus runUncertain(UncertainOperation operation) {
			ChangeExecutorImpl.this.contentPartFactory.getErrorHandler().isError(operation);
			return Status.OK_STATUS;
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
			return this.runUncertain(() -> this.change.apply());
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.runUncertain(() -> this.change.revert());
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return this.runUncertain(() -> this.change.apply());
		}
	}

}