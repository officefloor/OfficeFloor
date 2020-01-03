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

package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * Context for the {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnTranslatorContext<R, T> {

	/**
	 * Obtains the return value from the {@link Method}.
	 * 
	 * @return Return value from the {@link Method}.
	 */
	R getReturnValue();

	/**
	 * Specifies the translated return value.
	 * 
	 * @param value Translated return value.
	 */
	void setTranslatedReturnValue(T value) throws Exception;

	/**
	 * Obtains the {@link ManagedFunctionContext}.
	 * 
	 * @return {@link ManagedFunctionContext}.
	 */
	ManagedFunctionContext<?, ?> getManagedFunctionContext();

}
