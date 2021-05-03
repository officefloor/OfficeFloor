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

package net.officefloor.gef.editor;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import net.officefloor.model.change.Change;

/**
 * Listens to {@link Change} instances being executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChangeListener {

	/**
	 * Notified before the {@link ITransactionalOperation} is registered for
	 * execution.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void beforeTransactionOperation(ITransactionalOperation operation);

	/**
	 * Notified after the {@link ITransactionalOperation} is registered for
	 * execution.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void afterTransactionOperation(ITransactionalOperation operation);

	/**
	 * Notified pre-applying the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void preApply(Change<?> change);

	/**
	 * Notified post-applying the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void postApply(Change<?> change);

	/**
	 * Notified pre-reverting the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void preRevert(Change<?> change);

	/**
	 * Notified post-reverting the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void postRevert(Change<?> change);

}
