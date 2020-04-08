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

package net.officefloor.compile;

/**
 * Context for the {@link OfficeFloorCompilerConfigurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerConfigurerContext {

	/**
	 * Obtains the {@link OfficeFloorCompiler} being configured.
	 * 
	 * @return {@link OfficeFloorCompiler} being configured.
	 */
	OfficeFloorCompiler getOfficeFloorCompiler();

	/**
	 * <p>
	 * Allows specifying another {@link ClassLoader}.
	 * <p>
	 * To ensure {@link Class} compatibility, the input {@link ClassLoader} must be
	 * a child of the current {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * 
	 * @param classLoader {@link ClassLoader} that is child of
	 *                    {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * @throws IllegalArgumentException If not a child.
	 */
	void setClassLoader(ClassLoader classLoader) throws IllegalArgumentException;

}
