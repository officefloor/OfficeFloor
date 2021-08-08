/*-
 * #%L
 * OfficeCompiler
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
