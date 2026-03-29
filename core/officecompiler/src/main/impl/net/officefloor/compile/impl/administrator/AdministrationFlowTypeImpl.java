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

package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link AdministrationFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationFlowTypeImpl<F extends Enum<F>> implements AdministrationFlowType<F> {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Argument type to the {@link Flow}.
	 */
	private final Class<?> argumentType;

	/**
	 * Index identifying the {@link Flow}.
	 */
	private final int index;

	/**
	 * Key identifying the {@link Flow}.
	 */
	private final F key;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type to the {@link Flow}.
	 * @param index
	 *            Index identifying the {@link Flow}.
	 * @param key
	 *            Key identifying the {@link Flow}.
	 */
	public AdministrationFlowTypeImpl(String flowName, Class<?> argumentType, int index, F key) {
		this.flowName = flowName;
		this.argumentType = argumentType;
		this.index = index;
		this.key = key;
	}

	/*
	 * ==================== AdministrationFlowType =======================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}
