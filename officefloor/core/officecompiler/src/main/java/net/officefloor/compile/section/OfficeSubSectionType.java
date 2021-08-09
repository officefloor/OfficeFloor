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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSubSection;

/**
 * <code>Type definition</code> of a section of the {@link OfficeSubSection}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSubSectionType {

	/**
	 * Obtains the name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 * 
	 * @return Name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the parent {@link OfficeSubSectionType}.
	 * 
	 * @return Parent {@link OfficeSubSectionType} or <code>null</code> if this
	 *         is the {@link OfficeSectionType}.
	 */
	OfficeSubSectionType getParentOfficeSubSectionType();

	/**
	 * Obtains the {@link OfficeSubSectionType} instances of this
	 * {@link OfficeSectionType}.
	 * 
	 * @return {@link OfficeSubSectionType} instances.
	 */
	OfficeSubSectionType[] getOfficeSubSectionTypes();

	/**
	 * <p>
	 * Obtains the {@link OfficeFunctionType} instances for this particular
	 * {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeFunctionType} instances of the sub
	 * sections.
	 * 
	 * @return {@link OfficeFunctionType} instances for this particular
	 *         {@link OfficeSubSection}.
	 */
	OfficeFunctionType[] getOfficeFunctionTypes();

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionManagedObjectType} instances for this
	 * particular {@link OfficeSubSection}.
	 * <p>
	 * This does not include the {@link OfficeSectionManagedObjectType}
	 * instances of the sub sections.
	 * 
	 * @return {@link OfficeSectionManagedObjectType} instances for this
	 *         particular {@link OfficeSubSection}.
	 */
	OfficeSectionManagedObjectType[] getOfficeSectionManagedObjectTypes();

}
