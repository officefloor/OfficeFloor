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
 * Provides default implementation of methods for {@link ChangeListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeAdapter implements ChangeListener {

	@Override
	public void beforeTransactionOperation(ITransactionalOperation operation) {
	}

	@Override
	public void afterTransactionOperation(ITransactionalOperation operation) {
	}

	@Override
	public void preApply(Change<?> change) {
	}

	@Override
	public void postApply(Change<?> change) {
	}

	@Override
	public void preRevert(Change<?> change) {
	}

	@Override
	public void postRevert(Change<?> change) {
	}

}
