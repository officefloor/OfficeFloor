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
package net.officefloor.eclipse.editor.models;

import javax.inject.Inject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.model.change.Change;

/**
 * {@link Change} executor.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeExecutor {

	@Inject
	private IDomain domain;

	/**
	 * Obtains the {@link IDomain}.
	 * 
	 * @return {@link IDomain}.
	 */
	public IDomain getDomain() {
		return this.domain;
	}

	/**
	 * Executes the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	public void execute(Change<?> change) {
		try {
			this.domain.execute(new ChangeTransactionalOperation(change), null);
		} catch (ExecutionException ex) {
			// TODO tidy up handling to report the error
			System.err.print("FAILURE: executing change " + change.getChangeDescription() + " : ");
			ex.printStackTrace();
		}
	}

	/**
	 * Executes the {@link ITransactionalOperation}.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	public void execute(ITransactionalOperation operation) {
		try {
			this.domain.execute(operation, null);
		} catch (ExecutionException ex) {
			// TODO tidy up handling to report the error
			System.err.print("FAILURE: executing operation " + operation.getLabel() + " : ");
			ex.printStackTrace();
		}
	}

	/**
	 * {@link ITransactionalOperation} for the change.
	 */
	private static class ChangeTransactionalOperation extends AbstractOperation implements ITransactionalOperation {

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
			this.change.apply();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			this.change.revert();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			this.change.apply();
			return Status.OK_STATUS;
		}
	}

}