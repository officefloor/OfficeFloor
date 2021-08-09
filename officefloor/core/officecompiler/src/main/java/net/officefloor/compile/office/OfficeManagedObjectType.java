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

package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link ManagedObject} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectType {

	/**
	 * Obtains the name of the {@link OfficeObject} required by the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link OfficeObject} required by the {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains the fully qualified class name of the {@link Object} that must be
	 * returned from the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class name of the {@link Object} that must be
	 *         returned from the {@link ManagedObject}.
	 */
	String getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the fully qualified class names of the extension interfaces that
	 * must be supported by the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class names of the extension interfaces that must
	 *         be supported by the {@link ManagedObject}.
	 */
	String[] getExtensionInterfaces();

}
