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

package net.officefloor.plugin.clazz.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link ManagedFunctionFactory} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodFunctionFactory implements ManagedFunctionFactory<Indexed, Indexed> {

	/**
	 * {@link MethodObjectFactory}. Will be <code>null</code> if static
	 * {@link Method}.
	 */
	private final MethodObjectFactory methodObjectInstanceFactory;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final ClassDependencyFactory[] parameters;

	/**
	 * {@link MethodReturnTranslator} or <code>null</code>.
	 */
	private MethodReturnTranslator<Object, Object> returnTranslator;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      {@link Method} to invoke for the
	 *                                    {@link ManagedFunction}.
	 * @param parameters                  {@link ClassDependencyFactory} instances.
	 */
	public MethodFunctionFactory(MethodObjectFactory methodObjectInstanceFactory, Method method,
			ClassDependencyFactory[] parameters) {
		this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		this.method = method;
		this.parameters = parameters;
	}

	/**
	 * Specifies the {@link MethodReturnTranslator}.
	 * 
	 * @param returnTranslator {@link MethodReturnTranslator}.
	 */
	public void setMethodReturnTranslator(MethodReturnTranslator<Object, Object> returnTranslator) {
		this.returnTranslator = returnTranslator;
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * =============== ManagedFunctionFactory ===============
	 */

	@Override
	public MethodFunction createManagedFunction() {
		return new MethodFunction(this.methodObjectInstanceFactory, this.method, this.parameters,
				this.returnTranslator);
	}

}
