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
