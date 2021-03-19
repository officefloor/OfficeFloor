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

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Describes an object which the {@link ManagedObject} for the
 * {@link ManagedObjectSource} is dependent upon.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyMetaData<O extends Enum<O>> {

	/**
	 * Obtains the {@link Enum} key identifying this dependency. If
	 * <code>null</code> then dependency will be referenced by this instance's index
	 * in the array returned from {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link Enum} key identifying the dependency or <code>null</code>
	 *         indicating identified by an index.
	 */
	O getKey();

	/**
	 * Obtains the {@link Class} that the dependent object must extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

	/**
	 * <p>
	 * Obtains the annotations for the dependency.
	 * <p>
	 * This enables further description of required dependency.
	 * 
	 * @return Annotations for the dependency.
	 */
	Object[] getAnnotations();

	/**
	 * Provides a descriptive name for this dependency. This is useful to better
	 * describe the dependency.
	 * 
	 * @return Descriptive name for this dependency.
	 */
	String getLabel();

}
