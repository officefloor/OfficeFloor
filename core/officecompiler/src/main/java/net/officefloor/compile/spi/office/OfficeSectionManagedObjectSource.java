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

import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSource} contained within a {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectSource {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObjectSource}.
	 */
	String getOfficeSectionManagedObjectSourceName();

	/**
	 * Obtains the {@link OfficeSectionManagedObjectTeam} required by this
	 * {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @param teamName Name of the {@link ManagedObjectTeam}.
	 * @return {@link OfficeSectionManagedObjectTeam}.
	 */
	OfficeSectionManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName);

	/**
	 * Obtains the {@link OfficeSectionManagedObject} use of this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName Name of the {@link OfficeSectionManagedObject} to
	 *                          obtain.
	 * @return {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName);

}
