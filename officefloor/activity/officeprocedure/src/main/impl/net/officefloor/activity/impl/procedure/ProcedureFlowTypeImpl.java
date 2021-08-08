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

import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ProcedureFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureFlowTypeImpl implements ProcedureFlowType {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Argument type.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param flowName     Name of the {@link Flow}.
	 * @param argumentType Argument type.
	 */
	public ProcedureFlowTypeImpl(String flowName, Class<?> argumentType) {
		this.flowName = flowName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== ProcedureFlowType ===========================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}
