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

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFlowTypeImpl<F extends Enum<F>> implements ManagedObjectFlowType<F> {

	/**
	 * Name describing this flow.
	 */
	private final String name;

	/**
	 * Index identifying this flow.
	 */
	private final int index;

	/**
	 * Type of argument given to this flow.
	 */
	private final Class<?> argumentType;

	/**
	 * Key identifying this flow.
	 */
	private final F key;

	/**
	 * Initiate for a {@link ManagedObjectFlowType} invoked from a
	 * {@link ManagedFunction} added by the {@link ManagedObjectSource}.
	 * 
	 * @param index        Index identifying this flow.
	 * @param argumentType Type of argument given to this flow. May be
	 *                     <code>null</code>.
	 * @param key          Key identifying this flow. May be <code>null</code>.
	 * @param label        Label describing this flow. May be <code>null</code>.
	 */
	public ManagedObjectFlowTypeImpl(int index, Class<?> argumentType, F key, String label) {
		this.index = index;
		this.argumentType = argumentType;
		this.key = key;

		// Obtain the name for this flow
		if (!CompileUtil.isBlank(label)) {
			this.name = label;
		} else if (this.key != null) {
			this.name = this.key.toString();
		} else {
			this.name = String.valueOf(this.index);
		}
	}

	/*
	 * ====================== ManagedObjectFlowType ============================
	 */

	@Override
	public String getFlowName() {
		return this.name;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}
