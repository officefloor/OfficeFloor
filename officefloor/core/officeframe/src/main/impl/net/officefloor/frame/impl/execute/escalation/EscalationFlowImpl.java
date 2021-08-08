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

package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * Implementation of {@link EscalationFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationFlowImpl implements EscalationFlow {

	/**
	 * Type of cause handled by this {@link EscalationFlow}.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link ManagedFunctionMetaData} determine the actions for this
	 * {@link EscalationFlow}.
	 */
	private final ManagedFunctionMetaData<?, ?> taskMetaData;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData} determine the actions for this
	 *            {@link EscalationFlow}.
	 */
	public EscalationFlowImpl(Class<? extends Throwable> typeOfCause, ManagedFunctionMetaData<?, ?> taskMetaData) {
		this.typeOfCause = typeOfCause;
		this.taskMetaData = taskMetaData;
	}

	/*
	 * ======================== Escalation ====================================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData() {
		return this.taskMetaData;
	}

}
