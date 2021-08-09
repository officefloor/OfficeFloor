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

import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link SubSection} of an {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSubSection {

	/**
	 * Obtains the name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 * 
	 * @return Name of this {@link OfficeSection}/{@link OfficeSubSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the {@link OfficeSubSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSubSection} to obtain.
	 * @return {@link OfficeSubSection}.
	 */
	OfficeSubSection getOfficeSubSection(String sectionName);

	/**
	 * Obtains the {@link OfficeSectionFunction} instance particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param taskName
	 *            Name of the {@link OfficeSectionFunction} to obtain.
	 * @return {@link OfficeSectionFunction}.
	 */
	OfficeSectionFunction getOfficeSectionFunction(String taskName);

	/**
	 * Obtains the {@link OfficeSectionManagedObject} particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeSectionManagedObject} to obtain.
	 * @return {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName);

	/**
	 * Obtains the {@link OfficeSectionManagedObjectSource} particular to this
	 * {@link OfficeSubSection}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeSectionManagedObjectSource} to
	 *            obtain.
	 * @return {@link OfficeSectionManagedObjectSource}.
	 */
	OfficeSectionManagedObjectSource getOfficeSectionManagedObjectSource(String managedObjectSourceName);

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link OfficeSubSection}.
	 * <p>
	 * This enables providing {@link Governance} over all
	 * {@link OfficeSectionFunction} instances within the
	 * {@link OfficeSubSection} and all its subsequent {@link OfficeSubSection}
	 * instances.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	void addGovernance(OfficeGovernance governance);

}
