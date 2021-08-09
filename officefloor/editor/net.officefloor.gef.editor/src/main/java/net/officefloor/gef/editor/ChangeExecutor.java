/*-
 * #%L
 * [bundle] OfficeFloor Editor
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
