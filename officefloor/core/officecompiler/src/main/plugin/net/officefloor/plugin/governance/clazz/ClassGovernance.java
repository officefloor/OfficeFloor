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
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.plugin.clazz.method.MethodFunction;

/**
 * {@link Class} {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernance implements Governance<Object, Indexed> {

	/**
	 * Instance of the {@link Class}.
	 */
	private final Object instance;

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
	 * @param instance
	 *            Instance of the {@link Class}.
	 * @param governMethod
	 *            {@link Method} for governing the {@link ManagedObject}.
	 * @param enforceMethod
	 *            {@link Method} to enforce the {@link Governance}.
	 * @param disregardMethod
	 *            {@link Method} to disregard the {@link Governance}. May be
	 *            <code>null</code> if no functionality required for
	 *            disregarding.
	 */
	public ClassGovernance(Object instance, Method governMethod,
			Method enforceMethod, Method disregardMethod) {
		this.instance = instance;
		this.governMethod = governMethod;
		this.enforceMethod = enforceMethod;
		this.disregardMethod = disregardMethod;
	}

	/*
	 * ====================== Governance =========================
	 */

	@Override
	public void governManagedObject(Object extensionInterface,
			GovernanceContext<Indexed> context) throws Throwable {
		MethodFunction.invokeMethod(this.instance, this.governMethod,
				new Object[] { extensionInterface });
	}

	@Override
	public void enforceGovernance(GovernanceContext<Indexed> context)
			throws Throwable {
		MethodFunction
				.invokeMethod(this.instance, this.enforceMethod, new Object[0]);
	}

	@Override
	public void disregardGovernance(GovernanceContext<Indexed> context)
			throws Throwable {
		// Disregard if method to do so
		if (this.disregardMethod != null) {
			MethodFunction.invokeMethod(this.instance, this.disregardMethod,
					new Object[0]);
		}
	}

}
