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

package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;

/**
 * {@link BoundInputManagedObjectConfiguration} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class BoundInputManagedObjectConfigurationImpl implements
		BoundInputManagedObjectConfiguration {

	/**
	 * Input {@link ManagedObject} name.
	 */
	private final String inputManagedObjectName;

	/**
	 * Name of the {@link ManagedObjectSource} to bind to the input
	 * {@link ManagedObject}.
	 */
	private final String boundManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param inputManagedObjectName
	 *            Input {@link ManagedObject} name.
	 * @param boundManagedObjectSourceName
	 *            Name of the {@link ManagedObjectSource} to bind to the input
	 *            {@link ManagedObject}.
	 */
	public BoundInputManagedObjectConfigurationImpl(
			String inputManagedObjectName, String boundManagedObjectSourceName) {
		this.inputManagedObjectName = inputManagedObjectName;
		this.boundManagedObjectSourceName = boundManagedObjectSourceName;
	}

	/*
	 * =============== BoundInputManagedObjectConfiguration ====================
	 */

	@Override
	public String getInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public String getBoundManagedObjectSourceName() {
		return this.boundManagedObjectSourceName;
	}

}
