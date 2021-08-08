/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
