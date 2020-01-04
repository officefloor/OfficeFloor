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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Configuration for a dependent {@link Object} of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectConfiguration<O> {

	/**
	 * Indicates if this dependent {@link Object} is the argument passed to the
	 * {@link ManagedFunction}.
	 * 
	 * @return <code>true</code> if is argument passed to the
	 *         {@link ManagedFunction}. <code>false</code> indicates it is a
	 *         {@link ManagedObject} dependency.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the name of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * <p>
	 * This must return a value if not a parameter.
	 * 
	 * @return Name of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	String getScopeManagedObjectName();

	/**
	 * Obtains the type of {@link Object} required by the
	 * {@link ManagedFunction}.
	 * 
	 * @return Type of {@link Object} required by the {@link ManagedFunction}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the index identifying the dependent {@link Object}.
	 * 
	 * @return Index identifying the dependent {@link Object}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the dependent {@link Object}.
	 * 
	 * @return Key identifying the dependent {@link Object}. <code>null</code>
	 *         if indexed.
	 */
	O getKey();

}
