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
 * Executes {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChangeExecutor {

	/**
	 * Executes the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	void execute(Change<?> change);

	/**
	 * Executes the {@link ITransactionalOperation}.
	 * 
	 * @param operation
	 *            {@link ITransactionalOperation}.
	 */
	void execute(ITransactionalOperation operation);

	/**
	 * Adds a {@link ChangeListener}.
	 * 
	 * @param changeListener
	 *            {@link ChangeListener}.
	 */
	void addChangeListener(ChangeListener changeListener);

	/**
	 * Removes the {@link ChangeListener}.
	 * 
	 * @param changeListener
	 *            {@link ChangeListener}.
	 */
	void removeChangeListener(ChangeListener changeListener);

}
