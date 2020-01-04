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

package net.officefloor.compile.supplier;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a potentially supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectSourceType {

	/**
	 * Obtains the type of {@link Object} provided by the supplied
	 * {@link ManagedObject}.
	 * 
	 * @return Type of {@link Object} provided by the supplied
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the possible qualifier for the supplied {@link ManagedObject}.
	 * 
	 * @return Qualifier for the supplied {@link ManagedObject}. May be
	 *         <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, ?> getManagedObjectSource();

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	PropertyList getPropertyList();

}
