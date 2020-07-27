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
