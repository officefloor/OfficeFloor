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

package net.officefloor.plugin.clazz.constructor;

import java.lang.reflect.Constructor;

/**
 * Interrogates the {@link Class} for a {@link Constructor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogator {

	/**
	 * Interrogates for the {@link Constructor}.
	 * 
	 * @param context {@link ClassConstructorInterrogatorContext}.
	 * @return {@link Constructor}. Should be <code>null</code> to allow another
	 *         {@link ClassConstructorInterrogator} to find the {@link Constructor}.
	 * @throws Exception If fails to interrogate.
	 */
	Constructor<?> interrogate(ClassConstructorInterrogatorContext context) throws Exception;

}
