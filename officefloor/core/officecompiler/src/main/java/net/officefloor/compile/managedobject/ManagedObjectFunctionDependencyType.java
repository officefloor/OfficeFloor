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

package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a dependency required by a
 * {@link ManagedFunction} added by {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependencyType {

	/**
	 * Obtains the name of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Name of the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectName();

	/**
	 * Obtains the type of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Type of the {@link ManagedObjectFunctionDependency}.
	 */
	Class<?> getFunctionObjectType();

	/**
	 * Obtains the type qualifier for the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Type qualifier for the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectTypeQualifier();

}
