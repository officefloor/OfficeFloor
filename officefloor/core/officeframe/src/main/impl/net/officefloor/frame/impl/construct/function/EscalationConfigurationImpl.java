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

package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * Implementation of the {@link EscalationConfiguration}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationConfigurationImpl implements EscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link ManagedFunctionReference}.
	 */
	private final ManagedFunctionReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param taskNodeReference
	 *            {@link ManagedFunctionReference}.
	 */
	public EscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			ManagedFunctionReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * ================= EscalationConfiguration ========================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionReference getManagedFunctionReference() {
		return this.taskNodeReference;
	}

}
