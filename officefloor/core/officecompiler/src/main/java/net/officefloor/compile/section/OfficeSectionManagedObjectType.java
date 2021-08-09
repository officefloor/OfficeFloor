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

package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> for an {@link Office}
 * {@link SectionManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectType extends DependentObjectType {

	/**
	 * Obtains the name of this {@link Office} {@link SectionManagedObject}.
	 * 
	 * @return Name of this {@link Office} {@link SectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Obtains the supported extension interfaces by this {@link Office}
	 * {@link SectionManagedObject}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return Supported extension interfaces by this
	 *         {@link OfficeSectionManagedObject}.
	 */
	Class<?>[] getSupportedExtensionInterfaces();

	/**
	 * Obtains the {@link OfficeSectionManagedObjectSourceType} for this
	 * {@link OfficeSectionManagedObject}.
	 * 
	 * @return {@link OfficeSectionManagedObjectSourceType} for this
	 *         {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObjectSourceType getOfficeSectionManagedObjectSourceType();

}
