/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Dependency available to {@link ManagedFunction} instances added via
 * {@link ManagedObjectSourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependency {

	/**
	 * Obtains the name of this {@link ManagedFunction} dependency.
	 * 
	 * @return Name of this {@link ManagedFunction} dependency.
	 */
	String getFunctionDependencyName();

	/**
	 * Specifies qualifier for the type.
	 * 
	 * @param qualifier Type qualifier.
	 * @return <code>this</code> for fluent building.
	 */
	ManagedObjectFunctionDependency setTypeQualifier(String qualifier);

}
