/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link GovernanceEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceEscalationTypeImpl implements GovernanceEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of the {@link EscalationFlow}.
	 */
	private final Class<?> escalationType;

	/**
	 * Initiate.
	 * 
	 * @param escalationName
	 *            Name of {@link EscalationFlow}.
	 * @param escalationType
	 *            Type of the {@link EscalationFlow}.
	 */
	public GovernanceEscalationTypeImpl(String escalationName,
			Class<?> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =================== GovernanceEscalationType ==========================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

}
