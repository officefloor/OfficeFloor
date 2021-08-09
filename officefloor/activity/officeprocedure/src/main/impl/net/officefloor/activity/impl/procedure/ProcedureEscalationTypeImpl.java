/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link ProcedureEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEscalationTypeImpl implements ProcedureEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of {@link Escalation}.
	 */
	private final Class<? extends Throwable> escalationType;

	/**
	 * Instantiate.
	 * 
	 * @param escalationName Name of {@link EscalationFlow}.
	 * @param escalationType Type of {@link Escalation}.
	 */
	public ProcedureEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =============== ProcedureEscalationType ===================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	public Class<? extends Throwable> getEscalationType() {
		return this.escalationType;
	}
}
