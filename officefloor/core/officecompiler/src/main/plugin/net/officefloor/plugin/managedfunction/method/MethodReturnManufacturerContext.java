/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link MethodReturnManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnManufacturerContext<T> {

	/**
	 * Obtains the {@link Class} of the {@link Method} return.
	 * 
	 * @return {@link Class} of the {@link Method} return.
	 */
	Class<?> getReturnClass();

	/**
	 * <p>
	 * Overrides the return {@link Class} to the translated return {@link Class}.
	 * <p>
	 * Should this not be invoked, then the default {@link Method} return
	 * {@link Class} is used.
	 * 
	 * @param translatedReturnClass Translated return {@link Class}.
	 */
	void setTranslatedReturnClass(Class<? super T> translatedReturnClass);

	/**
	 * Obtains the {@link Annotation} instances for the {@link Method}.
	 * 
	 * @return {@link Annotation} instances for the {@link Method}.
	 */
	Annotation[] getMethodAnnotations();

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}