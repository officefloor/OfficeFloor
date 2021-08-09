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

package net.officefloor.plugin.clazz.interrogate;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Context for the {@link ClassInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassInjectionInterrogatorContext {

	/**
	 * <p>
	 * Obtains the {@link AnnotatedElement}.
	 * <p>
	 * Typically this is either a {@link Field} or {@link Method}.
	 * 
	 * @return {@link AnnotatedElement}.
	 */
	AnnotatedElement getAnnotatedElement();

	/**
	 * Obtains the {@link Class} of object for dependency injection.
	 * 
	 * @return {@link Class} of object for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Registers a injection point.
	 * <p>
	 * Should always be valid if passing in the return of
	 * {@link #getAnnotatedElement()}.
	 * 
	 * @param member {@link Field} or {@link Method} for injection.
	 * @throws IllegalArgumentException If invalid injection point type.
	 */
	void registerInjectionPoint(AnnotatedElement member) throws IllegalArgumentException;

	/**
	 * Registers a post construct {@link Method}.
	 * 
	 * @param method {@link Method} to be invoked after all dependencies are
	 *               injected.
	 */
	void registerPostConstruct(Method method);

}
