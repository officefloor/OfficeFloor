/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
