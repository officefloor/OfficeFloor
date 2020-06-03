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

package net.officefloor.plugin.clazz.dependency;

/**
 * Manufactures the {@link ClassDependencyFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyManufacturer {

	/**
	 * <p>
	 * Creates the {@link ClassDependencyFactory} for the particular dependency.
	 * <p>
	 * Should the {@link ClassDependencyManufacturer} not handled the dependency, it
	 * should return <code>null</code>. This is because the first
	 * {@link ClassDependencyManufacturer} providing a
	 * {@link ClassDependencyFactory} will be used.
	 * 
	 * @param context {@link ClassDependencyManufacturerContext}.
	 * @return {@link ClassDependencyFactory} or <code>null</code> if not able to
	 *         handle dependency.
	 * @throws Exception If fails to create the {@link ClassDependencyFactory}.
	 */
	ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception;

}
