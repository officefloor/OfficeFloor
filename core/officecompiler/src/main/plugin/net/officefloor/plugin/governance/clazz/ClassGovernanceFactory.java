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

package net.officefloor.plugin.governance.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Class} {@link GovernanceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernanceFactory implements GovernanceFactory<Object, Indexed> {

	/**
	 * {@link Class}.
	 */
	private final Class<?> clazz;

	/**
	 * {@link Method} for governing the {@link ManagedObject}.
	 */
	private final Method governMethod;

	/**
	 * {@link Method} to enforce the {@link Governance}.
	 */
	private final Method enforceMethod;

	/**
	 * {@link Method} to disregard the {@link Governance}.
	 */
	private final Method disregardMethod;

	/**
	 * Initiate.
	 * 
	 * @param clazz           {@link Class}.
	 * @param governMethod    {@link Method} for governing the
	 *                        {@link ManagedObject}.
	 * @param enforceMethod   {@link Method} to enforce the {@link Governance}.
	 * @param disregardMethod {@link Method} to disregard the {@link Governance}.
	 *                        May be <code>null</code> if no functionality required
	 *                        for disregarding.
	 */
	public ClassGovernanceFactory(Class<?> clazz, Method governMethod, Method enforceMethod, Method disregardMethod) {
		this.clazz = clazz;
		this.governMethod = governMethod;
		this.enforceMethod = enforceMethod;
		this.disregardMethod = disregardMethod;
	}

	/*
	 * ===================== GovernanceFactory ====================
	 */

	@Override
	public Governance<Object, Indexed> createGovernance() throws Throwable {

		// Instantiate the governance
		Object instance = this.clazz.getDeclaredConstructor().newInstance();

		// Create and return the governance
		return new ClassGovernance(instance, this.governMethod, this.enforceMethod, this.disregardMethod);
	}

}
