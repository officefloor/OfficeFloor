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

package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} contained within an {@link OfficeSubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObject extends DependentManagedObject,
		AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObject}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load
	 * this {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is
	 * the order they will be done.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting load
	 *            this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

}
