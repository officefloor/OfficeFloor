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

import java.lang.reflect.Constructor;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * {@link MethodObjectInstanceFactory} using the default {@link Constructor}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultConstructorMethodObjectInstanceFactory implements MethodObjectInstanceFactory {

	/**
	 * Arguments for the default {@link Constructor}.
	 */
	private static final Object[] DEFAULT_CONSTRUCTION_ARGUMENTS = new Object[0];

	/**
	 * Default constructor for {@link Class}.
	 */
	private final Constructor<?> constructor;

	/**
	 * Instantiate.
	 * 
	 * @param clazz {@link Class} to instantiate via default {@link Constructor}.
	 * @throws Exception If fails to obtain default {@link Constructor}.
	 */
	public DefaultConstructorMethodObjectInstanceFactory(Class<?> clazz) throws Exception {
		this.constructor = clazz.getConstructor(new Class[0]);
	}

	/*
	 * =================== MethodObjectInstanceFactory =======================
	 */

	@Override
	public Object createInstance(ManagedFunctionContext<Indexed, Indexed> context) throws Exception {
		return this.constructor.newInstance(DEFAULT_CONSTRUCTION_ARGUMENTS);
	}

}