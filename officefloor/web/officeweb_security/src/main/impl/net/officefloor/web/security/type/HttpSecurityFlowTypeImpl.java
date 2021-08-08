/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectFlowType;

/**
 * {@link HttpSecurityFlowType} adapted from the {@link ManagedObjectFlowType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityFlowTypeImpl<F extends Enum<F>> implements HttpSecurityFlowType<F> {

	/**
	 * {@link ManagedObjectFlowType}.
	 */
	private final ManagedObjectFlowType<F> flow;

	/**
	 * Initiate.
	 * 
	 * @param flow {@link ManagedObjectFlowType}.
	 */
	public HttpSecurityFlowTypeImpl(ManagedObjectFlowType<F> flow) {
		this.flow = flow;
	}

	/*
	 * ==================== HttpSecurityFlowType =======================
	 */

	@Override
	public String getFlowName() {
		return this.flow.getFlowName();
	}

	@Override
	public F getKey() {
		return this.flow.getKey();
	}

	@Override
	public int getIndex() {
		return this.flow.getIndex();
	}

	@Override
	public Class<?> getArgumentType() {
		return this.flow.getArgumentType();
	}
}
