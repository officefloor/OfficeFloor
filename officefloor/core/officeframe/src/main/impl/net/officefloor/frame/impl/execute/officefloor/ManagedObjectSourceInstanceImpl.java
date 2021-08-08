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

package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManagerFactory;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;

/**
 * {@link ManagedObjectSourceInstance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstanceImpl<F extends Enum<F>> implements ManagedObjectSourceInstance<F> {

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, F> managedObjectSource;

	/**
	 * {@link ManagedObjectExecuteManagerFactory} for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectExecuteManagerFactory<F> managedObjectExecuteContextFactory;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * {@link ManagedObjectServiceReady} instances.
	 */
	private final ManagedObjectServiceReady[] serviceReadiness;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSource                {@link ManagedObjectSource}.
	 * @param managedObjectExecuteContextFactory {@link ManagedObjectExecuteManagerFactory}
	 *                                           for the
	 *                                           {@link ManagedObjectSource}.
	 * @param managedObjectPool                  {@link ManagedObjectPool}.
	 * @param serviceReadiness                   {@link ManagedObjectServiceReady}
	 *                                           instances.
	 */
	public ManagedObjectSourceInstanceImpl(ManagedObjectSource<?, F> managedObjectSource,
			ManagedObjectExecuteManagerFactory<F> managedObjectExecuteContextFactory,
			ManagedObjectPool managedObjectPool, ManagedObjectServiceReady[] serviceReadiness) {
		this.managedObjectSource = managedObjectSource;
		this.managedObjectExecuteContextFactory = managedObjectExecuteContextFactory;
		this.managedObjectPool = managedObjectPool;
		this.serviceReadiness = serviceReadiness;
	}

	/*
	 * ==================== ManagedObjectSourceInstance ==================
	 */

	@Override
	public ManagedObjectSource<?, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectExecuteManagerFactory<F> getManagedObjectExecuteManagerFactory() {
		return this.managedObjectExecuteContextFactory;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	@Override
	public ManagedObjectServiceReady[] getServiceReadiness() {
		return this.serviceReadiness;
	}

}
