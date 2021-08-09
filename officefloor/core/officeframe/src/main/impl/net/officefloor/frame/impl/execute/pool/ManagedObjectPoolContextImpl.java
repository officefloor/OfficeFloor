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

package net.officefloor.frame.impl.execute.pool;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;

/**
 * {@link ManagedObjectPoolContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolContextImpl implements ManagedObjectPoolContext {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectPoolContextImpl(ManagedObjectSource<?, ?> managedObjectSource) {
		this.managedObjectSource = managedObjectSource;
	}

	/*
	 * ===================== ManagedObjectPoolContext ========================
	 */

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public boolean isCurrentThreadManaged() {
		return ManagedExecutionFactoryImpl.isCurrentThreadManaged();
	}

}
