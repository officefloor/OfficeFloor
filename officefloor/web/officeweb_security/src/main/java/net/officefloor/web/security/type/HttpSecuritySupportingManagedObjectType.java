/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of the
 * {@link HttpSecuritySupportingManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectType<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @return Name of the {@link HttpSecuritySupportingManagedObject}.
	 */
	String getSupportingManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<O, ?> getManagedObjectSource();

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	PropertyList getProperties();

	/**
	 * Obtains the object type for the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @return Object type.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link ManagedObjectScope}.
	 * 
	 * @return {@link ManagedObjectScope}.
	 */
	ManagedObjectScope getManagedObjectScope();

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObjectDependencyType}
	 * instances.
	 * 
	 * @return {@link HttpSecuritySupportingManagedObjectDependencyType} instances.
	 */
	HttpSecuritySupportingManagedObjectDependencyType<O>[] getDependencyTypes();

}
