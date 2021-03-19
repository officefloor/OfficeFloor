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
import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * Factory to create the {@link Object} instance to invoke the {@link Method}
 * on.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodObjectFactory {

	/**
	 * Creates the {@link Object} instance to invoke the {@link Method} on.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 * @return {@link Object} instance to invoke the {@link Method} on.
	 * @throws Throwable If fails to create the instance.
	 */
	Object createInstance(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

}
