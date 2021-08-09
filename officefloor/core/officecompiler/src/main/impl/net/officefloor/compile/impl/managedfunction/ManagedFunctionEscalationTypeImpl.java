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

package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link ManagedFunctionEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionEscalationTypeImpl
		implements ManagedFunctionEscalationType, ManagedFunctionEscalationTypeBuilder {

	/**
	 * Type of the {@link EscalationFlow}.
	 */
	private final Class<?> escalationType;

	/**
	 * Label of the {@link EscalationFlow}.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param escalationType Type of the {@link EscalationFlow}.
	 */
	public ManagedFunctionEscalationTypeImpl(Class<?> escalationType) {
		this.escalationType = escalationType;
	}

	/*
	 * =================== ManagedFunctionEscalationTypeBuilder ====================
	 */

	@Override
	public ManagedFunctionEscalationTypeBuilder setLabel(String label) {
		this.label = label;
		return this;
	}

	/*
	 * =================== ManagedFunctionEscalationType ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

	@Override
	public String getEscalationName() {
		// Obtain name by priorities
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.escalationType != null) {
			return this.escalationType.getName();
		} else {
			return "escalation";
		}
	}

}
